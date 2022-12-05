package kz.kcell.flow.revision.sap;

import kz.kcell.flow.files.Minio;
import kz.kcell.flow.revision.sap.SftpConfig;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service("createPRFile")
@Log
public class CreatePRFile implements JavaDelegate {

    private Minio minioClient;
    private SftpConfig.UploadGateway gateway;

    @Autowired
    public CreatePRFile(Minio minioClient, SftpConfig.UploadGateway uploadGateway) {
        this.minioClient = minioClient;
        this.gateway = uploadGateway;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        log.info("Pr file content is: " + content);

        String name = delegateExecution.getVariable("jrNumber") + "_Pr.txt";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;
        minioClient.saveFile(path, is, "text/plain");
        log.info("Saved to Minio Pr file: " + name);
        is.close();

        String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("prFile", SpinValues.jsonValue(json));
        log.info(" Pr variable added with content: " + json);
    }

}
