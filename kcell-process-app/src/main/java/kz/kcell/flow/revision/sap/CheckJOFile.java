package kz.kcell.flow.revision.sap;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Scanner;

@Service("checkJOFile")
@Log
public class CheckJOFile implements JavaDelegate {

    @Autowired
    private RemoteFileTemplate template;

    @Autowired
    private SftpConfig.UploadGateway sftpGateway;

    @Autowired
    private Environment environment;

    @Value("${sftp.remote.directory.jojr.ok:/KWMS_test/JR_JO_Creation/Processed_Sap_JO_File}")
    private String sftpRemoteDirectoryJoJrOk;

    @Value("${sftp.remote.directory.jojr.error:/KWMS_test/JR_JO_Creation/JO_Creation_Errors}")
    private String sftpRemoteDirectoryJoJrError;

    @Value("${s3.bucket.jojr:jojr}")
    private String jojrBucketName;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        SpinJsonNode joJrFile =  delegateExecution.<JsonValue>getVariableTyped("joJrFile").getValue();
        String name = joJrFile.prop("name").stringValue();

        String successFilePath = isSftp ? sftpRemoteDirectoryJoJrOk + "/ok_" + name : jojrBucketName + "/" + name;
        Boolean successResult = template.exists(successFilePath);

        if(successResult){
            if(isSftp){
                template.get(successFilePath,
                    inputStream -> {
                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";

                        log.info("Remote succes file for JR " + delegateExecution.getVariable("jrNumber").toString() + " is: " + result);
                    }
                );
            }
            delegateExecution.setVariable("joFileCheckResult", "success");
        } else {
            if(isSftp){
                String errorFilePath = sftpRemoteDirectoryJoJrError + "/" + name;
                Boolean errorResult = template.exists(errorFilePath);

                if(errorResult){
                    template.get(errorFilePath,
                        inputStream -> {
                            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                            String result = s.hasNext() ? s.next() : "";

                            log.info("Remote error file for JR " + delegateExecution.getBusinessKey() + " is: " + result);

                            String error = result.substring(result.lastIndexOf("\t"), result.length());
                            delegateExecution.setVariable("joFileCheckError", error);
                        }
                    );
                    template.remove(errorFilePath);
                    delegateExecution.setVariable("joFileCheckResult", "error");
                } else {
                    delegateExecution.setVariable("joFileCheckError", "JO result files not found");
                }
            } else {
                delegateExecution.setVariable("joFileCheckError", "JO files not found in bucket");
            }
            delegateExecution.setVariable("joFileCheckResult", "notFound");
        }
    }
}
