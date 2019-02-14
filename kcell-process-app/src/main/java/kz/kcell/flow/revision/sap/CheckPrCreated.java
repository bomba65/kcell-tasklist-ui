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

@Service("checkPrCreated")
@Log
public class CheckPrCreated implements JavaDelegate {

    @Autowired
    private RemoteFileTemplate template;

    @Autowired
    private Environment environment;

    @Value("${sftp.remote.directory.to.pr:/uploads/test/CIP_PR_Creation/PR_Waiting}")
    private String sftpRemoteDirectoryToPr;

    @Value("${s3.bucket.pr:prfiles}")
    private String prBucketName;

    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        SpinJsonNode prFile =  delegateExecution.<JsonValue>getVariableTyped("prFile").getValue();
        String name = prFile.prop("name").stringValue();
        String prFilePath = isSftp ? sftpRemoteDirectoryToPr + "/" + name : prBucketName + "/" + name;

        if(delegateExecution.hasVariable("prFileCheckResult")){
            String prFileCheckResult = delegateExecution.getVariable("prFileCheckResult").toString();

            if("success".equals(prFileCheckResult)){
                delegateExecution.setVariable("prCreated", "Yes");
            } else {
                if(template.exists(prFilePath)){
                    template.remove(prFilePath);
                    delegateExecution.setVariable("prCreated", "No");
                } else {
                    delegateExecution.setVariable("prCreated", "Yes");
                }
            }
        } else {
            delegateExecution.setVariable("prCreated", "No");
        }
    }
}
