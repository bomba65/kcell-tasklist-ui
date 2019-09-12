package kz.kcell.flow.files;

import io.minio.errors.*;
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
import java.util.stream.Stream;

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
    public void notify(DelegateExecution delegateExecution) {

        Stream<String> fileVars = Stream.of(this.fileVars.getValue(delegateExecution).toString().split(",")).map(String::trim).filter(s -> !s.isEmpty());

        SpinList<SpinJsonNode> filesListToHistory = new SpinListImpl<>();

        fileVars.forEach(fileVarName -> {
            if(delegateExecution.hasVariable(fileVarName)){
                SpinJsonNode files = delegateExecution.<JsonValue>getVariableTyped(fileVarName).getValue();
                if (files.isArray()) {
                    String piId = delegateExecution.getProcessInstanceId();

                    SpinList<SpinJsonNode> filesList = files.elements();
                    filesList.forEach(file -> {
                        String tempPath = file.prop("path").stringValue();
                        String name = file.prop("name").stringValue();
                        String permPath = piId + "/" + name;
                        try {
                            minioClient.moveToPermanentStorage(tempPath, permPath);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to move object to permanent storage", e);
                        }
                        file.prop("path", permPath);

                        filesListToHistory.add(file);
                    });

                    delegateExecution.setVariable(fileVarName, SpinValues.jsonValue(filesList.toString()));
                } else {
                    String piId = delegateExecution.getProcessInstanceId();

                    String tempPath = files.prop("path").stringValue();
                    String name = files.prop("name").stringValue();
                    String permPath = piId + "/" + name;
                    try {
                        minioClient.moveToPermanentStorage(tempPath, permPath);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to move object to permanent storage", e);
                    }
                    files.prop("path", permPath);

                    filesListToHistory.add(files);

                    delegateExecution.setVariable(fileVarName, SpinValues.jsonValue(files.toString()));
                }

                SpinJsonNode resolutionContainer = delegateExecution.<JsonValue>getVariableTyped("resolutions").getValue();
                if(resolutionContainer.isArray() && resolutionContainer.elements().size() > 0){
                    SpinList<SpinJsonNode> resolutions = delegateExecution.<JsonValue>getVariableTyped("resolutions").getValue().elements();
                    resolutions.forEach(resolution -> {
                        resolution.prop("files", SpinJsonNode.JSON(filesListToHistory.toString()));
                    });
                    delegateExecution.setVariable("resolutions", SpinValues.jsonValue(resolutions.toString()));
                }
            }
        });

    }
}
