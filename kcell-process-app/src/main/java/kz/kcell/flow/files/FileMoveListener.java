package kz.kcell.flow.files;

import io.minio.errors.*;
import kz.kcell.flow.minio.Minio;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static org.camunda.spin.Spin.JSON;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class FileMoveListener implements ExecutionListener {

    Expression fileVars;

    private Minio minioClient;

    @Autowired
    public FileMoveListener(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void notify(DelegateExecution delegateExecution) throws InvalidKeyException, InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException,
    NoResponseException, ErrorResponseException, InternalException, IOException, XmlPullParserException,
    InvalidArgumentException {

        Stream<String> fileVars = Stream.of(this.fileVars.getValue(delegateExecution).toString().split(",")).map(String::trim).filter(s -> !s.isEmpty());

        fileVars.forEach(fileVarName -> {
            if(delegateExecution.getVariableTyped(fileVarName)!=null && delegateExecution.getVariableTyped(fileVarName).getValue()!=null){

                JsonValue filesVar = delegateExecution.getVariableTyped(fileVarName);

                SpinJsonNode files = filesVar.getValue();
                if (files.isArray()) {
                    String piId = delegateExecution.getProcessInstanceId();

                    SpinList<SpinJsonNode> filesList = files.elements();
                    filesList.forEach(f -> {
                        String tempPath = f.prop("path").stringValue();
                        String name = f.prop("name").stringValue();
                        String permPath = piId + "/" + name;
                        try {
                            minioClient.moveToPermanentStorage(tempPath, permPath);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to move object to permanent storage", e);
                        }

                        f.prop("path", permPath);
                    });

                    String s = filesList.toString();
                    delegateExecution.setVariable(fileVarName, SpinValues.jsonValue(s));
                }
            }
        });

    }
}
