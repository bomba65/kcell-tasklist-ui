package kz.kcell.flow.revision.sap;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service("createJOFile")
@Log
public class CreateJOFile implements JavaDelegate {

    private Minio minioClient;

    @Autowired
    public CreateJOFile(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        log.info("JoJr content is: " + content);

        String name = delegateExecution.getVariable("jrNumber") + "_JoJr.txt";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;

        minioClient.saveFile(path, is, "text/plain");
        log.info("Saved to Minio JoJr file: " + name);
        is.close();

        String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("joJrFile", SpinValues.jsonValue(json));
        log.info(" JoJr variable added with content: " + json);
    }
}
