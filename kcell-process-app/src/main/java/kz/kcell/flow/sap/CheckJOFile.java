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
import org.springframework.util.FileCopyUtils;

import java.util.Scanner;

@Service("checkJOFile")
@Log
public class CheckJOFile implements JavaDelegate {

    @Autowired
    private SftpRemoteFileTemplate template;

    @Value("${sftp.remote.directory.jojr.ok:/home/KWMS/JR_JO_Creation/Processed Sap JO File}")
    private String sftpRemoteDirectoryJoJrOk;

    @Value("${sftp.remote.directory.jojr.error:/home/KWMS/JR_JO_Creation/JO Creation Errors}")
    private String sftpRemoteDirectoryJoJrError;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        SpinJsonNode joJrFile =  delegateExecution.<JsonValue>getVariableTyped("joJrFile").getValue();
        String name = joJrFile.prop("name").stringValue();

        String successFilePath = sftpRemoteDirectoryJoJrOk + "/ok_" + name;
        Boolean successResult = template.exists(successFilePath);

        if(successResult){
            template.get(successFilePath,
                inputStream -> {
                    Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                    String result = s.hasNext() ? s.next() : "";

                    log.info("Remote succes file for JR " + delegateExecution.getVariable("jrNumber").toString() + " is: " + result);
                }
            );
            delegateExecution.setVariable("joFileCheckResult", "succes");
        } else {
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
            } else {
                delegateExecution.setVariable("joFileCheckError", "Файлы для провекри создания JoJr не найдены");
            }

            delegateExecution.setVariable("joFileCheckResult", "error");
        }
    }
}
