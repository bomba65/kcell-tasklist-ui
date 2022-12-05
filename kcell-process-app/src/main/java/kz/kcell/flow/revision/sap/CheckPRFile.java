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
import java.util.StringTokenizer;

@Service("checkPRFile")
@Log
public class CheckPRFile implements JavaDelegate {

    @Autowired
    private RemoteFileTemplate template;

    @Autowired
    private Environment environment;

    @Value("${sftp.remote.directory.pr.ok:/KWMS_test/CIP_PR_Creation/PR_Created}")
    private String sftpRemoteDirectoryPrOk;

    @Value("${sftp.remote.directory.pr.error:/KWMS_test/CIP_PR_Creation/PR_Didnt_Created}")
    private String sftpRemoteDirectoryPrError;

    @Value("${s3.bucket.pr:prfiles}")
    private String prBucketName;

    @Override
    public void execute(DelegateExecution delegateExecution){

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        SpinJsonNode prFile =  delegateExecution.<JsonValue>getVariableTyped("prFile").getValue();
        String name = prFile.prop("name").stringValue();

        String successFilePath = isSftp ? sftpRemoteDirectoryPrOk + "/" + name : prBucketName + "/" + name;
        Boolean successResult = template.exists(successFilePath);

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
            delegateExecution.setVariable("prFileCheckResult", "success");
        } else {
            if(isSftp){
                String errorFilePath = sftpRemoteDirectoryPrError + "/" + name;
                Boolean errorResult = template.exists(errorFilePath);

                if(errorResult){
                    template.get(errorFilePath,
                        inputStream -> {
                            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                            String result = s.hasNext() ? s.next() : "";

                            StringBuilder str = new StringBuilder("");
                            StringTokenizer byNewLine = new StringTokenizer(result, "\n");
                            while(byNewLine.hasMoreTokens()){
                                String line = byNewLine.nextToken();
                                int jrNumberfirstPos = ordinalIndexOf(line, "\t", 2);
                                int jrNumberlastPos = ordinalIndexOf(line, "\t", 3);
                                String jrNumber = line.substring(jrNumberfirstPos, jrNumberlastPos);
                                String error = line.substring(line.lastIndexOf("\t"));
                                str.append(jrNumber);
                                str.append(": ");
                                str.append(error);
                                str.append("\n");
                            }

                            delegateExecution.setVariable("prFileCheckError", str.toString());
                            delegateExecution.setVariable("prFileCheckResult", "error");
                        }
                    );
                    template.remove(errorFilePath);
                } else {
                    delegateExecution.setVariable("prFileCheckError", "Pr result files not found");
                    delegateExecution.setVariable("prFileCheckResult", "notFound");
                }
            } else {
                delegateExecution.setVariable("prFileCheckError", "PR files not found in bucket");
                delegateExecution.setVariable("prFileCheckResult", "notFound");
            }
        }
    }

    private int ordinalIndexOf(String str, String substr, int n) {
        int pos = str.indexOf(substr);
        while (--n > 0 && pos != -1)
            pos = str.indexOf(substr, pos + 1);
        return pos;
    }
}
