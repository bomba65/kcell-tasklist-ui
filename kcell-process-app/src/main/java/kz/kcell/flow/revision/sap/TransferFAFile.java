package kz.kcell.flow.revision.sap;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service("transferFAFile")
@Log
public class TransferFAFile implements JavaDelegate {

    private Minio minioClient;
    private SftpConfig.UploadGateway gateway;

    @Autowired
    public TransferFAFile(Minio minioClient, SftpConfig.UploadGateway gateway) {
        this.minioClient = minioClient;
        this.gateway = gateway;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        SpinJsonNode faFile =  delegateExecution.<JsonValue>getVariableTyped("faFile").getValue();
        String name = faFile.prop("name").stringValue();

        String tmpDir = System.getProperty("java.io.tmpdir");
        File file = new File(tmpDir+ "/" + name);

        String path = faFile.prop("path").stringValue();
        InputStream is = minioClient.getObject(path);

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(IOUtils.toByteArray(is));
        fos.close();
        is.close();

        gateway.uploadFa(file);
        log.info("Fa " + name + " uploaded to remote server");

        file.delete();
    }
}
