package kz.kcell.flow.files;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service("createFAFile")
@Log
public class CreateFAFile implements JavaDelegate {

    private Minio minioClient;

    @Autowired
    public CreateFAFile(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        log.info("FA content is: " + content);

        String name = delegateExecution.getVariable("jrNumber") + "_FA.txt";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;

        minioClient.saveFile(path, is, "text/plain");
        log.info("Saved to Minio FA file: " + name);
        is.close();

        String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("faFile", SpinValues.jsonValue(json));
        log.info(" FA variable added with content: " + json);


    }
}
