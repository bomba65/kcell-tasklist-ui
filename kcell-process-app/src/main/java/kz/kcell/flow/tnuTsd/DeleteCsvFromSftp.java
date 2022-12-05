package kz.kcell.flow.tnuTsd;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
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

import java.util.Arrays;

@Log
@Service("tnuTsdDeleteCsvFromSftp")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DeleteCsvFromSftp implements JavaDelegate {
    private Minio minioClient;
    private String sftpUrl;
    private String sftpLogin;
    private String sftpPassword;
    private int sftpPort = 22;

    @Autowired
    private Environment environment;

    @Autowired
    public DeleteCsvFromSftp(@Value("${tnuTsd.sftp.server:192.168.196.163}") String sftpUrl,
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

        if(isSftp){
            SpinJsonNode tnuTsdSftpFile = delegateExecution.<JsonValue>getVariableTyped("tnuTsdSftpFile").getValue();
            String name = tnuTsdSftpFile.prop("name").stringValue();

            ChannelSftp channel = SetupConnection();

            channel.connect();

            if( channel.lstat("camunda/" + name) != null){
                channel.rm("camunda/" + name);
            }

            if(channel.isConnected()){
                channel.exit();
                channel.disconnect();
            }
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
