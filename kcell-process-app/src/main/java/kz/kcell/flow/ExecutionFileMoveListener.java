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
    public void notify(DelegateExecution delegateExecution) {

        String[] fileVars = delegateExecution.getProcessEngineServices()
            .getRepositoryService()
            .getBpmnModelInstance(delegateExecution.getProcessDefinitionId())
            .getModelElementsByType(Process.class)
            .stream()
            .map(Process::getExtensionElements)
            .filter(Objects::nonNull)
            .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
            .flatMap(e -> e.getCamundaProperties().stream())
            .filter(e -> e.getCamundaName().equals("fileVars"))
            .map(CamundaProperty::getCamundaValue)
            .findAny()
            .orElse("")
            .split(",");

        ObjectMapper mapper = new ObjectMapper();

        try{
            for (String fileName: fileVars){
                if(!"".equals(fileName) && delegateExecution.getVariableTyped(fileName)!=null && delegateExecution.getVariableTyped(fileName).getValue()!=null){

                    JsonValue file = delegateExecution.getVariableTyped(fileName);

                    if(file.getValue().hasProp("isNew")){
                        Boolean isNew = file.getValue().prop("isNew").boolValue();

                        if(isNew && file.getValue().hasProp("path")){
                            String path = file.getValue().prop("path").stringValue();
                            String name = file.getValue().prop("name").stringValue();

                            minioClient.copyObject(minioClient.getTempBucketName(), path, minioClient.getBucketName(), delegateExecution.getProcessInstanceId() + "/" + name);
                            file.getValue().prop("path", delegateExecution.getProcessInstanceId() + "/" + name);
                            file.getValue().prop("isNew", false);

                            delegateExecution.setVariable(fileName, file);

                            minioClient.removeObject(minioClient.getTempBucketName(), path);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
