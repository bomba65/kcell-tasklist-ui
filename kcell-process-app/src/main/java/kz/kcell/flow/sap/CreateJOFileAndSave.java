package kz.kcell.flow.sap;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.script.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

@Service("createJOFileAndSave")
@Log
public class CreateJOFileAndSave implements JavaDelegate {

    private Minio minioClient;
    private SftpConfig.UploadGateway gateway;

    @Autowired
    public CreateJOFileAndSave(Minio minioClient, SftpConfig.UploadGateway uploadGateway) {
        this.minioClient = minioClient;
        this.gateway = uploadGateway;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        log.info("content:" + content);

        String name = delegateExecution.getVariable("jrNumber") + "_JoJr.txt";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;
        minioClient.saveFile(path, is, "text/plain");
        is.close();

        String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("joJrFile", SpinValues.jsonValue(json));

        String tmpDir = System.getProperty("java.io.tmpdir");

        File file = new File(tmpDir+ "/" + name);

        ByteArrayInputStream iis = new ByteArrayInputStream(content.getBytes("utf-8"));
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(IOUtils.toByteArray(iis));
        fos.close();
        iis.close();

        gateway.uploadJrJo(file);

        file.delete();
    }
}
