package kz.kcell.flow.sap;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service("createSapPr")
@Log
public class CreateSapPR implements JavaDelegate {

    private Minio minioClient;

    @Autowired
    public CreateSapPR(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        log.info("content:" + content);

        String path = delegateExecution.getProcessInstanceId() + "/" + delegateExecution.getVariable("jrNumber") + "_Pr.txt";
        minioClient.saveFile(path, is, "text/plain");

        String json = "{\"name\" : \"" + delegateExecution.getVariable("jrNumber") + "_Pr.txt" + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("prFile", SpinValues.jsonValue(json));

        is.close();
    }

}
