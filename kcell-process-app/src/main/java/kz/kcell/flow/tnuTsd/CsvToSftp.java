package kz.kcell.flow.tnuTsd;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;

@Log
@Service("tnuTsdCsvToSftp")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CsvToSftp implements JavaDelegate {
    private Minio minioClient;
    private String sftpUrl;
    private String sftpLogin;
    private String sftpPassword;
    private int sftpPort = 22;
    private Session session = null;

    @Autowired
    private Environment environment;

    @Autowired
    public CsvToSftp(Minio minioClient,
                    @Value("${tnuTsd.sftp.server:192.168.196.163}") String sftpUrl,
                    @Value("${tnuTsd.sftp.login:kwms-camunda}") String sftpLogin,
                    @Value("${tnuTsd.sftp.password:Zaq12345}") String sftpPassword) {
        this.minioClient = minioClient;
        this.sftpUrl = sftpUrl;
        this.sftpLogin = sftpLogin;
        this.sftpPassword = sftpPassword;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        String name = delegateExecution.getVariable("tnuTsdNumber") + ".csv";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;

        minioClient.saveFile(path, is, "text/plain");
        is.close();

        String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("tnuTsdSftpFile", SpinValues.jsonValue(json));

        if(isSftp){
            String tmpDir = System.getProperty("java.io.tmpdir");
            File file = new File(tmpDir+ "/" + name);

            InputStream inputStream = minioClient.getObject(path);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(IOUtils.toByteArray(inputStream));

            ChannelSftp channel = SetupConnection();

            channel.connect();

            channel.put(tmpDir+ "/" + name, "camunda/");

            channel.exit();

            if(channel.isConnected()){
                channel.exit();
                channel.disconnect();
            }

            fos.close();
            file.delete();
        }
    }
    private ChannelSftp SetupConnection() throws JSchException{
        JSch jsch = new JSch();

        Session session = jsch.getSession(sftpLogin, sftpUrl);
        session.setPassword(sftpPassword);

        session.setConfig("StrictHostKeyChecking", "no");

        if(!session.isConnected()){
            session.connect();
        }

        return (ChannelSftp) session.openChannel("sftp");
    }
}
