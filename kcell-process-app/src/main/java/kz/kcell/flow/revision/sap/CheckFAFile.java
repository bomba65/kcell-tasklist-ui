package kz.kcell.flow.revision.sap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.BpmnError;
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
import java.util.StringTokenizer;

@Service("checkFAFile")
@Log
public class CheckFAFile implements JavaDelegate {

    @Autowired
    private RemoteFileTemplate template;

    @Autowired
    private SftpConfig.UploadGateway sftpGateway;

    @Autowired
    private Environment environment;

    @Value("${sftp.remote.directory.from.fa:/KWMS_test/FA_Geting/Fixed_Asset}")
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

        if(successResult){
            if(isSftp){
                template.get(successFilePath,
                    inputStream -> {
                        boolean success = true;
                        SpinList<SpinJsonNode> sapFaList = new SpinListImpl<>();

                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";

                        log.info("Remote succes file for Fa " + delegateExecution.getVariable("jrNumber").toString() + " is: ");
                        log.info(result);

                        StringTokenizer byNewLine = new StringTokenizer(result,"\n");
                        int newLineTokenLength = byNewLine.countTokens();

                        log.info("faResults length: " + newLineTokenLength);

                        for(int i=0; i<newLineTokenLength; i++){
                            StringTokenizer byTab = new StringTokenizer(byNewLine.nextToken(),"\t");
                            int tabTokenLength = byTab.countTokens();

                            log.info("faDataRow length: " + tabTokenLength);

                            if(tabTokenLength > 2){
                                ObjectMapper mapper = new ObjectMapper();
                                ObjectNode sapFa = mapper.createObjectNode();
                                sapFa.put("faClass", byTab.nextToken());
                                sapFa.put("sloc", byTab.nextToken());
                                sapFa.put("faNumber", byTab.nextToken());
                                if(tabTokenLength>3 && "Absent!".equals(byTab.nextToken())){
                                    sapFa.put("absent", true);
                                    success = false;
                                }
                                JsonValue jsonValue = SpinValues.jsonValue(sapFa.toString()).create();
                                sapFaList.add(jsonValue.getValue());
                            }
                        }
                        delegateExecution.setVariable("sapFaList", SpinValues.jsonValue(sapFaList.toString()));
                        if(success){
                            delegateExecution.setVariable("faFileCheckResult", "success");
                        } else {
                            throw new BpmnError("error", "Fa code absent!");
                        }
                    }
                );
            } else {
                SpinList<SpinJsonNode> sapFaList = new SpinListImpl<>();
                delegateExecution.setVariable("sapFaList", SpinValues.jsonValue(sapFaList.toString()));
                throw new BpmnError("error", "Test error fa file!");
            }
        } else {
            delegateExecution.setVariable("faFileCheckResult", "notFound");
        }
    }
}
