package kz.kcell.flow.tnuTsd;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

@Log
@Service("tnuTsdCsvToFtp")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CsvToFtp implements JavaDelegate {

    private Minio minioClient;
    private String ftpUrl;
    private String ftpLogin;
    private String ftpPassword;
    private int ftpPort = 21;

    @Autowired
    private Environment environment;

    @Autowired
    public CsvToFtp(Minio minioClient,
                    @Value("${tnuTsd.ftp.server:192.168.210.235}") String ftpUrl,
                    @Value("${tnuTsd.ftp.login:rrldb}") String ftpLogin,
                    @Value("${tnuTsd.ftp.password:Zaq12345}") String ftpPassword) {
        this.minioClient = minioClient;
        this.ftpUrl = ftpUrl;
        this.ftpLogin = ftpLogin;
        this.ftpPassword = ftpPassword;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        String content = String.valueOf(delegateExecution.getVariableLocal("content"));
        ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes("utf-8"));

        log.info("FA content is: " + content);

        String name = delegateExecution.getVariable("tnuTsdNumber") + ".csv";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;

        minioClient.saveFile(path, is, "text/plain");
        is.close();

        if(isSftp){
            String tmpDir = System.getProperty("java.io.tmpdir");
            File file = new File(tmpDir+ "/" + name);

            InputStream inputStream = minioClient.getObject(path);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(IOUtils.toByteArray(inputStream));

            InputStream fileInputStream = new FileInputStream(file);

            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(ftpUrl, ftpPort);
            ftpClient.login(ftpLogin, ftpPassword);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            log.info("file.length(): " + file.length());

            boolean completed = ftpClient.storeFile( "VKC-MENTUM-EW/rrldb/" + name, fileInputStream);

            if (!completed) {
                throw new RuntimeException("File not uploaded to ftp server + " + ftpUrl + " file path: " + name);
            }

            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            fos.close();
            file.delete();
        }
    }
}
