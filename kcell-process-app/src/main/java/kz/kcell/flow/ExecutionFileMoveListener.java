package kz.kcell.flow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.minio.MinioClient;
import io.minio.errors.*;
import kz.kcell.flow.minio.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import javax.script.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Service
@Log
public class ExecutionFileMoveListener implements ExecutionListener {

    @Autowired
    Minio minioClient;

    @Override
    public void notify(DelegateExecution delegateExecution) throws InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
    NoResponseException, ErrorResponseException, InternalException, IOException, XmlPullParserException,
    InvalidArgumentException {

        String[] fileVars = delegateExecution.getProcessEngineServices()
            .getRepositoryService()
            .getBpmnModelInstance(delegateExecution.getProcessDefinitionId())
            .getModelElementsByType(Process.class)
            .stream()
            .map(Process::getExtensionElements)
            .filter(Objects::nonNull)
            .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
            .flatMap(e -> e.getCamundaProperties().stream())
            .filter(Objects::nonNull)
            .filter(e -> e.getCamundaName().equals("fileVars"))
            .map(CamundaProperty::getCamundaValue)
            .findAny()
            .orElse("")
            .split(",");

        ObjectMapper mapper = new ObjectMapper();

        for (String fileName: fileVars){
            if(!"".equals(fileName) && delegateExecution.getVariableTyped(fileName)!=null && delegateExecution.getVariableTyped(fileName).getValue()!=null){

                JsonValue file = delegateExecution.getVariableTyped(fileName);

                if(file.getValue().hasProp("isNew")){
                    Boolean isNew = file.getValue().prop("isNew").boolValue();

                    if(isNew && file.getValue().hasProp("files") && file.getValue().prop("files").isArray()) {
                        SpinList<SpinJsonNode> newFiles = new SpinListImpl<>();
                        SpinList<SpinJsonNode> files = file.getValue().prop("files").elements();
                        for (SpinJsonNode node: files) {
                            if(node.hasProp("value") && node.prop("value").hasProp("path")) {
                                SpinJsonNode value = node.prop("value");
                                String path = value.prop("path").stringValue();
                                String name = node.prop("name").stringValue();

                                minioClient.copyObject(minioClient.getTempBucketName(), path, minioClient.getBucketName(), delegateExecution.getProcessInstanceId() + "/" + name);
                                value.prop("path", delegateExecution.getProcessInstanceId() + "/" + name);
                                node.prop("value", value);

                                minioClient.removeObject(minioClient.getTempBucketName(), path);
                                newFiles.add(node);
                            }
                        }

                        System.out.println("size: " + newFiles.size());

                        file.getValue().prop("isNew", false);
                        file.getValue().prop("files", newFiles.stream().collect(toList()));
                        delegateExecution.setVariable(fileName, file);
                    }
                }
            }
        }
    }
}
