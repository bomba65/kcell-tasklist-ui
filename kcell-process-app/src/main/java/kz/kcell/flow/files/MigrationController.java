package kz.kcell.flow.files;

import io.minio.MinioClient;
import io.minio.errors.*;
import org.camunda.bpm.engine.*;
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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
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

    @RequestMapping(value = "/files/migrate/{processDefinitionId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> migrateFiles(@PathVariable("processDefinitionId") String processDefinitionId, HttpServletRequest request) throws InvalidBucketNameException, NoSuchAlgorithmException, InsufficientDataException, IOException,
        InvalidKeyException, NoResponseException, XmlPullParserException, ErrorResponseException,
        InternalException, InvalidEndpointException, InvalidPortException,
        InvalidArgumentException {

        MinioClient minioClient = new MinioClient(this.minioUrl, this.minioAccessKey, this.minioSecretKey, "us-east-1");

        List<Execution> executions = runtimeService.createExecutionQuery().processDefinitionId(processDefinitionId).list();

        for (Execution e:executions){
            JsonValue supplementaryFiles = runtimeService.getVariableTyped(e.getId(), "supplementaryFiles");

            if(supplementaryFiles!=null && supplementaryFiles.getValue()!=null){

                Map<String, FileValue> map = new HashMap<>();
                for (int i=1;i<=5;i++){
                    FileValue supplementaryFile = runtimeService.getVariableTyped(e.getId(), "supplementaryFile"+i);
                    if(supplementaryFile!=null && supplementaryFile.getValue()!=null){
                        map.put(supplementaryFile.getFilename(), supplementaryFile);
                    }
                }
                if(supplementaryFiles.getValue().hasProp("files") && supplementaryFiles.getValue().prop("files").isArray()){
                    SpinList<SpinJsonNode> elements = supplementaryFiles.getValue().prop("files").elements();

                    for (SpinJsonNode node: elements) {
                        if(node.hasProp("value") && node.prop("value").hasProp("path")){
                            String name = node.prop("name").stringValue();
                            String path = node.prop("value").prop("path").stringValue();

                            FileValue file = map.get(name);
                            if(file!=null){
                                String mimeType = file.getMimeType();

                                minioClient.putObject(minio.getBucketName(), path, file.getValue(), mimeType);
                            }
                        }
                    }
                }
            }

/*            JsonValue trFiles = runtimeService.getVariableTyped(e.getId(), "trFiles");
            JsonValue trFilesName = runtimeService.getVariableTyped(e.getId(), "trFilesName");

            if (trFiles!=null && trFiles.getValue()!=null && trFilesName!=null && trFilesName.getValue()!=null){

                Map<String, Map<String, String>> trFilesNameMap = new HashMap<>();
                if(trFilesName.getValue().isArray()) {
                    SpinList<SpinJsonNode> elements = trFilesName.getValue().elements();
                    for (SpinJsonNode node: elements) {
                        Map<String, String> properties = new HashMap<>();
                        if(node.hasProp("name")){
                            properties.put("name", node.prop("name").stringValue());
                        }
                        if(node.hasProp("value")){
                            SpinJsonNode value = node.prop("value");
                            if(value.hasProp("path")){
                                properties.put("path", value.prop("path").stringValue());
                            }
                            if(value.hasProp("description")){
                                properties.put("description", value.prop("description").stringValue());
                            }
                            if(value.hasProp("name")){
                                properties.put("filename", value.prop("name").stringValue());
                            }
                        }
                        trFilesNameMap.put(properties.get("name"), properties);
                    }
                }

                if(trFiles.getValue().isArray()){
                    SpinList<SpinJsonNode> elements = trFiles.getValue().elements();

                    for (SpinJsonNode node: elements){
                        if(node.hasProp("id")){
                            String id = node.prop("id").stringValue();

                            Map<String, String> properties = null;

                            for (Map.Entry<String, Map<String, String>> entry : trFilesNameMap.entrySet()) {
                                if (id.equals(entry.getValue().get("description"))){
                                    properties = entry.getValue();
                                }
                            }

                            if(properties!=null){
                                FileValue trFile = runtimeService.getVariableTyped(e.getId(), properties.get("name"));
                                if(trFile!=null && trFile.getValue()!=null){
                                    InputStream fileContent = trFile.getValue();
                                    String mimeType = trFile.getMimeType();

                                    minioClient.putObject(minio.getBucketName(), properties.get("path"), fileContent, mimeType);
                                }
                            }
                        }
                    }
                }
            }
            runtimeService.setVariable(e.getId(), "trFilesOld", trFilesName);
            runtimeService.removeVariable(e.getId(), "trFiles");


            String[] vars = new String[] {"actOfMaterialsDispatchingFile", "tssrssidFile", "eLicenseResolutionFile", "sapPRFileXLS", "kcellWarehouseMaterialsList",
                "contractorZIPWarehouseMaterialsList", "supplementaryFile1", "supplementaryFile2", "supplementaryFile3", "supplementaryFile4", "supplementaryFile5"};

            for (String variable : vars){
                if (runtimeService.getVariableTyped(e.getId(), variable) instanceof FileValue) {
                    FileValue oldVariable = runtimeService.getVariableTyped(e.getId(), variable);
                    JsonValue newVariable = runtimeService.getVariableTyped(e.getId(), variable + "Name");

                    if (oldVariable != null && oldVariable.getValue() != null && newVariable != null && newVariable.getValue() != null) {
                        InputStream fileContent = oldVariable.getValue();
                        String mimeType = oldVariable.getMimeType();

                        String path = newVariable.getValue().prop("path").stringValue();

                        minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                    }
                }
            }

            JsonValue siteWorksFiles = runtimeService.getVariableTyped(e.getId(), "siteWorksFiles");
            if(siteWorksFiles!=null && siteWorksFiles.getValue()!=null){
                SpinList<SpinJsonNode> elements = siteWorksFiles.getValue().elements();
                for (SpinJsonNode node: elements) {
                    if(node.hasProp("name")){
                        FileValue oldFile = runtimeService.getVariableTyped(e.getId(), node.prop("name").stringValue());
                        if(oldFile!=null && oldFile.getValue()!=null){
                            InputStream fileContent = oldFile.getValue();
                            String mimeType = oldFile.getMimeType();
                            String path = node.prop("value").prop("path").stringValue();

                            minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                        }
                    }
                }
            }
*/
        }

/*
        List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery().finished().processDefinitionId("Revision:9:9d66a48a-a08a-11e7-90e3-0242ac130008").list();

        for (HistoricProcessInstance hp: processInstances){
           List<HistoricVariableInstance> trFilesList = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName("trFiles").list();
           List<HistoricVariableInstance> trFilesNameList = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName("trFilesName").list();

            Map<String, Map<String, String>> trFilesNameMap = new HashMap<>();
           for(HistoricVariableInstance v: trFilesNameList){
               SpinJsonNode trFilesName = (SpinJsonNode) v.getTypedValue().getValue();

               if(trFilesName.isArray()) {
                   SpinList<SpinJsonNode> elements = trFilesName.elements();
                   for (SpinJsonNode node: elements) {
                       Map<String, String> properties = new HashMap<>();
                       if(node.hasProp("name")){
                           properties.put("name", node.prop("name").stringValue());
                       }
                       if(node.hasProp("value")){
                           SpinJsonNode value = node.prop("value");
                           if(value.hasProp("path")){
                               properties.put("path", value.prop("path").stringValue());
                           }
                           if(value.hasProp("description")){
                               properties.put("description", value.prop("description").stringValue());
                           }
                           if(value.hasProp("name")){
                               properties.put("filename", value.prop("name").stringValue());
                           }
                       }
                       trFilesNameMap.put(properties.get("name"), properties);
                   }
               }
           }
           for(HistoricVariableInstance v: trFilesList){
               JsonValue trFiles = (JsonValue) v;

               if(trFiles.getValue().isArray()){
                   SpinList<SpinJsonNode> elements = trFiles.getValue().elements();

                   for (SpinJsonNode node: elements){
                       if(node.hasProp("id")){
                           String id = node.prop("id").stringValue();

                           Map<String, String> properties = null;

                           for (Map.Entry<String, Map<String, String>> entry : trFilesNameMap.entrySet()) {
                               System.out.println(entry.getKey() + "/" + entry.getValue());
                               if (id.equals(entry.getValue().get("description"))){
                                   properties = entry.getValue();
                               }
                           }

                           if(properties!=null){
                               FileValue trFile = runtimeService.getVariableTyped(v.getName(), properties.get("name"));
                               if(trFile!=null && trFile.getValue()!=null){
                                   InputStream fileContent = trFile.getValue();
                                   String mimeType = trFile.getMimeType();

                                   minioClient.putObject(minio.getBucketName(), properties.get("path"), fileContent, mimeType);
                               }
                           }
                       }
                   }
               }
           }

            String[] vars = new String[] {"actOfMaterialsDispatchingFile", "tssrssidFile", "eLicenseResolutionFile", "sapPRFileXLS", "kcellWarehouseMaterialsList",
                "contractorZIPWarehouseMaterialsList", "supplementaryFile1", "supplementaryFile2", "supplementaryFile3", "supplementaryFile4", "supplementaryFile5"};

            for (String variable : vars){
                List<HistoricVariableInstance> varInstance = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName(variable).list();

                for (HistoricVariableInstance v:varInstance){
                    if (v instanceof FileValue) {
                        FileValue oldVariable = (FileValue) v;
                        JsonValue newVariable = null;

                        List<HistoricVariableInstance> newVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName(variable + "Name").list();
                        for (HistoricVariableInstance n:newVariables){
                            newVariable = (JsonValue) n;
                        }

                        if (oldVariable.getValue() != null && newVariable != null && newVariable.getValue() != null) {
                            InputStream fileContent = oldVariable.getValue();
                            String mimeType = oldVariable.getMimeType();

                            String path = newVariable.getValue().prop("path").stringValue();
                            minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);
                    }
                }
            }

            List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName("siteWorksFiles").list();
            for (HistoricVariableInstance v:variables) {
                SpinJsonNode siteWorksFiles = (SpinJsonNode) v.getTypedValue().getValue();

                if(siteWorksFiles!=null){
                    SpinList<SpinJsonNode> elements = siteWorksFiles.elements();
                    for (SpinJsonNode node: elements) {
                        if(node.hasProp("name")){
                            List<HistoricVariableInstance> oldFiles = historyService.createHistoricVariableInstanceQuery().processInstanceId(hp.getId()).variableName(node.prop("name").stringValue()).list();

                            for (HistoricVariableInstance o:oldFiles) {
                                FileValue oldFile = (FileValue) o.getTypedValue();

                                if (oldFile != null) {
                                    InputStream fileContent = oldFile.getValue();
                                    String mimeType = oldFile.getMimeType();
                                    String path = node.prop("value").prop("path").stringValue();

                                    minioClient.putObject(minio.getBucketName(), path, fileContent, mimeType);

                                }
                            }
                        }
                    }
                }

            }
        }
*/
        return ResponseEntity.ok("Succes");
    }

    /*
        actOfMaterialsDispatchingFile - actOfMaterialsDispatchingFileName
        tssrssidFile - tssrssidFileName
        eLicenseResolutionFile - eLicenseResolutionFileName
        sapPRFileXLS - sapPRFileXLSName
        kcellWarehouseMaterialsList - kcellWarehouseMaterialsListName
        contractorZIPWarehouseMaterialsList - contractorZIPWarehouseMaterialsListName
        supplementaryFile1 - in supplementaryFiles
        supplementaryFile2 - in supplementaryFiles
        supplementaryFile3 - in supplementaryFiles
        supplementaryFile4 - in supplementaryFiles
        supplementaryFile5 - in supplementaryFiles
        works_{{$index}}_file_{{work.files.length}} - worksFiles

        jrBlank -
        sapTransferRequestFile -
     */
}
