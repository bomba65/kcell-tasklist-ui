package kz.kcell.flow.files;

import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
@Log
@RequestMapping("/migrate")
public class MigrationController {

    private final String minioAccessKey;
    private final String minioSecretKey;
    private final String minioUrl;

    @Autowired
    private Minio minio;

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    HistoryService historyService;

    @Autowired
    public MigrationController(@Value("${minio.url:http://localhost:9000}") String minioUrl,
                               @Value("${minio.access.key:AKIAIOSFODNN7EXAMPLE}") String minioAccessKey,
                               @Value("${minio.secret.key:wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY}") String minioSecretKey)  {
        this.minioAccessKey = minioAccessKey;
        this.minioSecretKey = minioSecretKey;
        this.minioUrl = minioUrl;
    }

    @RequestMapping(value = "/files/migrate", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> migrateFiles(HttpServletRequest request) throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, IOException,
        InvalidKeyException, NoResponseException, XmlPullParserException, ErrorResponseException,
        InternalException, InvalidEndpointException, InvalidPortException,
        InvalidArgumentException {

        MinioClient minioClient = new MinioClient(this.minioUrl, this.minioAccessKey, this.minioSecretKey, "us-east-1");

        List<Execution> executions = runtimeService.createExecutionQuery().processDefinitionKey("Revision").list();

        for (Execution e:executions){
            log.info("+++++++++++++++++++++++++++++++++++++++++++++");
            log.info("e: " + e.getId());
            String[] vars = new String[] {"actOfMaterialsDispatchingFile", "tssrssidFile", "eLicenseResolutionFile", "sapPRFileXLS", "kcellWarehouseMaterialsList", "contractorZIPWarehouseMaterialsList"};

            for (String variable : vars){
                if (runtimeService.getVariableTyped(e.getId(), variable)!=null && runtimeService.getVariableTyped(e.getId(), variable) instanceof FileValue) {
                    FileValue oldVariable = runtimeService.getVariableTyped(e.getId(), variable);
                    JsonValue newVariable = runtimeService.getVariableTyped(e.getId(), variable + "Name");

                    if (oldVariable != null && oldVariable.getValue() != null && newVariable != null && newVariable.getValue() != null && newVariable.getValue().hasProp("path")) {
                        InputStream fileContent = oldVariable.getValue();
                        String mimeType = oldVariable.getMimeType();

                        String path = newVariable.getValue().prop("path").stringValue();

                        minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                    }
                }
            }

            JsonValue siteWorksJsonVariable = runtimeService.<JsonValue>getVariableTyped(e.getId(), "siteWorksFiles");
            if(siteWorksJsonVariable!=null){
                SpinJsonNode siteWorksFiles = siteWorksJsonVariable.getValue();
                if(siteWorksFiles.isArray()){
                    SpinList<SpinJsonNode> workList = siteWorksFiles.elements();
                    for (SpinJsonNode work:workList){
                        String name = work.prop("name").stringValue();
                        String path = work.prop("value").prop("path").stringValue();

                        FileValue workFile = runtimeService.<FileValue>getVariableTyped(e.getId(), name);
                        if(workFile!=null){
                            InputStream fileContent = workFile.getValue();
                            String mimeType = workFile.getMimeType();
                            minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                        }
                    }
                }
            }

            JsonValue trJsonVariable = runtimeService.<JsonValue>getVariableTyped(e.getId(), "trFilesName");
            if(trJsonVariable!=null){
                SpinJsonNode trFilesName = trJsonVariable.getValue();
                log.info("trFilesName: " + trFilesName);
                if(trFilesName.isArray()){
                    SpinList<SpinJsonNode> trList = trFilesName.elements();

                    Map<String, FileValue> trFiles = new HashMap<>();
                    for(int i=0;i<trFilesName.elements().size();i++){
                        FileValue trFile = runtimeService.<FileValue>getVariableTyped(e.getId(), "trFile"+i);
                        if(trFile!=null){
                            trFiles.put(trFile.getFilename(), trFile);
                        }
                    }
                    if(trFiles.size()>0){
                        for (SpinJsonNode tr:trList){
                            String name = tr.prop("name").stringValue();
                            String path = tr.prop("path").stringValue();

                            log.info("name: " + name);
                            log.info("path: " + path);

                            if(trFiles.get(name)!=null){
                                InputStream fileContent = trFiles.get(name).getValue();
                                String mimeType = trFiles.get(name).getMimeType();
                                log.info("fileName: " + trFiles.get(name).getFilename());
                                log.info("mimeType: " + mimeType);
                                minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                            }
                        }
                    }
                }
            }

/*            JsonValue supplementaryFiles = runtimeService.getVariableTyped(e.getId(), "supplementaryFiles");

            if(supplementaryFiles!=null && supplementaryFiles.getValue()!=null){

                Map<String, FileValue> map = new HashMap<>();
                for (int i=1;i<=5;i++){
                    FileValue supplementaryFile = runtimeService.getVariableTyped(e.getId(), "supplementaryFile"+i);
                    if(supplementaryFile!=null && supplementaryFile.getValue()!=null){
                        map.put(supplementaryFile.getFilename(), supplementaryFile);
                    }
                }
                if(supplementaryFiles.getValue().isArray()){
                    SpinList<SpinJsonNode> elements = supplementaryFiles.getValue().elements();

                    for (SpinJsonNode node: elements) {
                        if(node.hasProp("path")){
                            String name = node.prop("name").stringValue();
                            String path = node.prop("path").stringValue();

                            FileValue file = map.get(name);
                            if(file!=null){
                                String mimeType = file.getMimeType();

                                minioClient.putObject(minio.getBucketName(), path, file.getValue(), mimeType);
                            }
                        }
                    }
                }
            }
*/
        }


        List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery().finished().processDefinitionKey("Revision").list();

        for (HistoricProcessInstance hp: processInstances){
            log.info("=======================================");
            String[] vars = new String[] {"actOfMaterialsDispatchingFile", "tssrssidFile", "eLicenseResolutionFile", "sapPRFileXLS", "kcellWarehouseMaterialsList", "contractorZIPWarehouseMaterialsList"};

            for (String variable : vars) {
                List<HistoricVariableInstance> hVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName(variable).list();
                if(hVariables.size()>0){
                    HistoricVariableInstance hVariable = hVariables.get(0);

                    List<HistoricVariableInstance> hVariableNames = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName(variable+"Name").list();
                    if(hVariableNames.size()>0 && ((JsonValue) hVariableNames.get(0).getTypedValue()).getValue().hasProp("path")) {
                        InputStream fileContent = ((FileValue) hVariable.getTypedValue()).getValue();
                        String mimeType = ((FileValue) hVariable.getTypedValue()).getMimeType();
                        HistoricVariableInstance hVariableName = hVariableNames.get(0);

                        String path = ((JsonValue)hVariableName.getTypedValue()).getValue().prop("path").stringValue();

                        minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                    }
                }
            }

            List<HistoricVariableInstance> hVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName("siteWorksFiles").list();
            if(hVariables.size()>0){
                SpinJsonNode siteWorksFiles = ((JsonValue) hVariables.get(0).getTypedValue()).getValue();

                if(siteWorksFiles.isArray()){
                    SpinList<SpinJsonNode> workList = siteWorksFiles.elements();
                    for (SpinJsonNode work:workList){
                        String name = work.prop("name").stringValue();
                        String path = work.prop("value").prop("path").stringValue();

                        List<HistoricVariableInstance> hWorkVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName(name).list();
                        if(hWorkVariables.size()>0){
                            InputStream fileContent = ((FileValue) hWorkVariables.get(0).getTypedValue()).getValue();
                            String mimeType = ((FileValue) hWorkVariables.get(0).getTypedValue()).getMimeType();
                            minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                        }
                    }
                }
            }

            List<HistoricVariableInstance> trVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName("trFilesName").list();
            if(trVariables.size()>0){
                SpinJsonNode trFilesName = ((JsonValue) trVariables.get(0).getTypedValue()).getValue();
                log.info("trFilesName: " + trFilesName);
                if(trFilesName.isArray()) {
                    SpinList<SpinJsonNode> trList = trFilesName.elements();
                    Map<String, FileValue> trFiles = new HashMap<>();
                    for(int i=0;i<trFilesName.elements().size();i++){
                        List<HistoricVariableInstance> hTrFileVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName("trFile"+i).list();
                        if(hTrFileVariables.size()>0){
                            FileValue trFile = ((FileValue) hTrFileVariables.get(0).getTypedValue());
                            trFiles.put(trFile.getFilename(), trFile);
                        }
                    }
                    if(trFiles.size()>0){
                        for (SpinJsonNode tr:trList) {
                            String name = tr.prop("name").stringValue();
                            String path = tr.prop("path").stringValue();

                            log.info("name: " + name);
                            log.info("path: " + path);

                            if(trFiles.get(name)!=null){
                                InputStream fileContent = trFiles.get(name).getValue();
                                String mimeType = trFiles.get(name).getMimeType();
                                log.info("fileName: " + trFiles.get(name).getFilename());
                                log.info("mimeType: " + mimeType);
                                minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                            }
                        }
                    }
                }
            }
        }
        return ResponseEntity.ok("Succes");
    }

    /*
        actOfMaterialsDispatchingFile - actOfMaterialsDispatchingFileName
        tssrssidFile - tssrssidFileName
        eLicenseResolutionFile - eLicenseResolutionFileName
        sapPRFileXLS - sapPRFileXLSName
        kcellWarehouseMaterialsList - kcellWarehouseMaterialsListName
        contractorZIPWarehouseMaterialsList - contractorZIPWarehouseMaterialsListName
        works_{{$index}}_file_{{work.files.length}} - worksFiles

        jrBlank -
        sapTransferRequestFile -
     */
}
