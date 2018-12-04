package kz.kcell.flow.sap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.integration.file.remote.RemoteFileTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Scanner;

@Service("checkFAFile")
@Log
public class CheckFAFile implements JavaDelegate {

    @Autowired
    private RemoteFileTemplate template;

    @Autowired
    private SftpConfig.UploadGateway sftpGateway;

    @Autowired
    private Environment environment;

    @Value("${sftp.remote.directory.from.fa:/home/KWMS/FA_Geting/Fixed_Asset}")
    private String sftpRemoteDirectoryFromFa;

    @Value("${s3.bucket.fa:fafiles}")
    private String faBucketName;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

        SpinJsonNode faFile =  delegateExecution.<JsonValue>getVariableTyped("faFile").getValue();
        String name = faFile.prop("name").stringValue();

        String successFilePath = isSftp ? sftpRemoteDirectoryFromFa + "/" + name : faBucketName + "/" + name;
        Boolean successResult = template.exists(successFilePath);

        String reason = delegateExecution.getVariable("reason").toString();
        JsonValue jobWorks = delegateExecution.<JsonValue>getVariableTyped( "jobWorks");

        if(successResult){
            if(isSftp){
                template.get(successFilePath,
                    inputStream -> {
                        boolean success = true;
                        SpinList<SpinJsonNode> sapFaList = new SpinListImpl<>();

                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";

                        log.info("Remote succes file for Fa " + delegateExecution.getVariable("jrNumber").toString() + " is: " + result);

                        String[] faResults = result.split("/n");
                        for(String faResult: faResults){
                            String[] faDataRow = faResult.split("/t");
                            ObjectMapper mapper = new ObjectMapper();
                            ObjectNode sapFa = mapper.createObjectNode();
                            sapFa.put("faClass", faDataRow[0]);
                            sapFa.put("sloc", faDataRow[1]);
                            sapFa.put("faNumber", faDataRow[2]);
                            if(faDataRow.length>3 && "Absent!".equals(faDataRow[3])){
                                sapFa.put("absent", true);
                                success = false;
                            }
                            JsonValue jsonValue = SpinValues.jsonValue(sapFa.toString()).create();
                            sapFaList.add(jsonValue.getValue());
                        }
                        if(success){
                            delegateExecution.setVariable("faFileCheckResult", "success");
                        } else {
                            delegateExecution.setVariable("faFileCheckResult", "error");
                        }
                        delegateExecution.setVariable("sapFaList", SpinValues.jsonValue(sapFaList.toString()));
                    }
                );
            } else {
                SpinList<SpinJsonNode> sapFaList = new SpinListImpl<>();
                delegateExecution.setVariable("sapFaList", SpinValues.jsonValue(sapFaList.toString()));
                delegateExecution.setVariable("faFileCheckResult", "error");
            }
        } else {
            delegateExecution.setVariable("faFileCheckResult", "notFound");
        }
    }
}
