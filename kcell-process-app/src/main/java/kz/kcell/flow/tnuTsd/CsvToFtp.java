package kz.kcell.flow.tnuTsd;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;

@Log
public class CsvToFtp implements ExecutionListener {

    private Minio minioClient;

    @Autowired
    public CsvToFtp(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        log.info("FA content is: " + content);

        String name = delegateExecution.getVariable("tnuTsdNumber") + ".csv";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;

        minioClient.saveFile(path, is, "text/plain");
        log.info("Saved to Minio CSV file: " + name);
        is.close();
    }
}
