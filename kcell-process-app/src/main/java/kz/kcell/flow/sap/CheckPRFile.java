package kz.kcell.flow.sap;

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

@Service("checkPRFile")
@Log
public class CheckPRFile implements JavaDelegate {

    @Autowired
    private RemoteFileTemplate template;

    @Autowired
    private Environment environment;

    @Value("${sftp.remote.directory.pr.ok:/home/KWMS/CIP_PR_Creation/PR_Created}")
    private String sftpRemoteDirectoryPrOk;

    @Value("${sftp.remote.directory.pr.error:/home/KWMS/CIP_PR_Creation/PR_Didnt_Created}")
    private String sftpRemoteDirectoryPrError;

    @Value("${sftp.remote.directory.pr.number:/home/KWMS/CIP_PR_Creation/PR_Status/status.txt}")
    private String sftpRemoteDirectoryPrNumber;

    @Value("${s3.bucket.pr:prfiles}")
    private String prBucketName;

    @Override
    public void execute(DelegateExecution delegateExecution){

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("production")));

        SpinJsonNode prFile =  delegateExecution.<JsonValue>getVariableTyped("prFile").getValue();
        String name = prFile.prop("name").stringValue();

        String successFilePath = isSftp ? sftpRemoteDirectoryPrOk + "/" + name : prBucketName + "/" + name;
        Boolean successResult = template.exists(successFilePath);

        log.info("isSftp :" + isSftp);
        log.info("successFilePath :" + successFilePath);
        log.info("successResult :" + successResult);

        if(successResult){
            if(isSftp) {
                template.get(successFilePath,
                    inputStream -> {
                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";

                        log.info("Remote succes file for JR " + delegateExecution.getVariable("jrNumber").toString() + " is: " + result);
                    }
                );
            }
/*            Boolean numberResult = template.exists(sftpRemoteDirectoryPrNumber);
            if(numberResult){
                template.get(sftpRemoteDirectoryPrNumber,
                    inputStream -> {
                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";

                        boolean statusFound = false;
                        String[] statuses = result.split("\n");
                        for(String status:statuses){
                            String jrNumber = status.substring(status.indexOf("\t")+1,status.lastIndexOf("\t"));
                            if(delegateExecution.getVariable("jrNumber").toString().equals(jrNumber)){
                                delegateExecution.setVariable("prGeneratedKey", status.substring(0,t.indexOf("\t")));
                                delegateExecution.setVariable("prFileCheckResult", "succes");
                                statusFound = true;
                                break;
                            }
                        }

                        if(statusFound){
                            delegateExecution.setVariable("prFileCheckError", "Pr status not found");
                            delegateExecution.setVariable("prFileCheckResult", "error");
                        }
                    }
                );
            } else {
                delegateExecution.setVariable("prFileCheckError", "Pr status file not found");
                delegateExecution.setVariable("prFileCheckResult", "error");
            }
*/
            delegateExecution.setVariable("prFileCheckResult", "succes");
        } else {
            if(isSftp){
                String errorFilePath = sftpRemoteDirectoryPrError + "/" + name;
                Boolean errorResult = template.exists(errorFilePath);

                if(errorResult){
                    template.get(errorFilePath,
                        inputStream -> {
                            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                            String result = s.hasNext() ? s.next() : "";

                            delegateExecution.setVariable("prFileCheckError", result);
                        }
                    );
                    template.remove(errorFilePath);
                } else {
                    delegateExecution.setVariable("prFileCheckError", "Pr result files not found");
                }
            } else {
                delegateExecution.setVariable("prFileCheckError", "PR files not found in bucket");
            }

            delegateExecution.setVariable("prFileCheckResult", "error");
        }
    }
}
