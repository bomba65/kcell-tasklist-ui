package kz.kcell.flow.sap;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service("checkPRFile")
@Log
public class CheckPRFile implements JavaDelegate {

    @Autowired
    private SftpRemoteFileTemplate template;

    @Value("${sftp.remote.directory.pr.ok:/home/KWMS/CIP_PR_Creation/PR_Created}")
    private String sftpRemoteDirectoryPrOk;

    @Value("${sftp.remote.directory.pr.error:/home/KWMS/CIP_PR_Creation/PR_Didnt_Created}")
    private String sftpRemoteDirectoryPrError;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        SpinJsonNode prFile =  delegateExecution.<JsonValue>getVariableTyped("prFile").getValue();
        String name = prFile.prop("name").stringValue();

        String successFilePath = sftpRemoteDirectoryPrOk + "/" + name;
        Boolean successResult = template.exists(successFilePath);

        if(successResult){
            template.get(successFilePath,
                inputStream -> {
                    Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";

                    log.info("Remote succes file for JR " + delegateExecution.getVariable("jrNumber").toString() + " is: " + result);
                }
            );
            delegateExecution.setVariable("prFileCheckResult", "succes");
        } else {
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
                delegateExecution.setVariable("prFileCheckError", "Файлы для проверки создания Pr не найдены");
            }

            delegateExecution.setVariable("prFileCheckResult", "error");
        }
    }
}
