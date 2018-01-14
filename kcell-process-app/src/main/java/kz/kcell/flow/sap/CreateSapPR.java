package kz.kcell.flow.sap;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;

@Service("createSapPr")
@Log
public class CreateSapPR implements JavaDelegate {

    private Minio minioClient;
    private SftpConfig.UploadGateway gateway;

    @Autowired
    public CreateSapPR(Minio minioClient, SftpConfig.UploadGateway uploadGateway) {
        this.minioClient = minioClient;
        this.gateway = uploadGateway;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        log.info("content:" + content);

        String name = delegateExecution.getVariable("jrNumber") + "_Pr.txt";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;
        minioClient.saveFile(path, is, "text/plain");

        String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("prFile", SpinValues.jsonValue(json));

        String tmpDir = System.getProperty("java.io.tmpdir");

        File file = new File(tmpDir + name);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(IOUtils.toByteArray(is));
        fos.close();
        is.close();

        gateway.uploadPr(file);

        file.delete();
    }

}
