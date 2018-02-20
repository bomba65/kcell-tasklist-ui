package kz.kcell.flow.sap;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service("transferJOFile")
@Log
public class TransferJOFile implements JavaDelegate {

    private Minio minioClient;
    private SftpConfig.UploadGateway gateway;

    @Autowired
    public TransferJOFile(Minio minioClient, SftpConfig.UploadGateway gateway) {
        this.minioClient = minioClient;
        this.gateway = gateway;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        SpinJsonNode joJrFile =  delegateExecution.<JsonValue>getVariableTyped("joJrFile").getValue();
        String name = joJrFile.prop("name").stringValue();

        String tmpDir = System.getProperty("java.io.tmpdir");
        File file = new File(tmpDir+ "/" + name);

        String path = joJrFile.prop("path").stringValue();
        InputStream is = minioClient.getObject(path);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(IOUtils.toByteArray(is));
        fos.close();
        is.close();

        gateway.uploadJrJo(file);
        log.info("JoJr " + name + " uploaded to remote server");

        file.delete();
    }
}
