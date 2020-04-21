package kz.kcell.flow.tnuTsd;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service("tnuTsdDeleteCsvFromFtp")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeleteCsvFromFtp implements JavaDelegate {

    private String ftpUrl;
    private String ftpLogin;
    private String ftpPassword;
    private int ftpPort = 21;

    @Autowired
    private Environment environment;

    @Autowired
    public DeleteCsvFromFtp(@Value("${tnuTsd.ftp.server:192.168.210.235}") String ftpUrl,
                    @Value("${tnuTsd.ftp.login:rrldb}") String ftpLogin,
                    @Value("${tnuTsd.ftp.password:Zaq12345}") String ftpPassword) {
        this.ftpUrl = ftpUrl;
        this.ftpLogin = ftpLogin;
        this.ftpPassword = ftpPassword;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        if(isSftp){
            SpinJsonNode tnuTsdFtpFile = delegateExecution.<JsonValue>getVariableTyped("tnuTsdFtpFile").getValue();
            String name = tnuTsdFtpFile.prop("name").stringValue();

            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(ftpUrl, ftpPort);
            ftpClient.login(ftpLogin, ftpPassword);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            int length = ftpClient.listFiles("VKC-MENTUM-EW/rrldb/" + name).length;
            if(length > 0){
                boolean completed = ftpClient.deleteFile("VKC-MENTUM-EW/rrldb/" + name);
                if (!completed) {
                    throw new RuntimeException("File not deleted from ftp server + " + ftpUrl + " file path: " + name);
                }
            }

            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
