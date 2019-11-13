package kz.kcell.flow.dismantleReplace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import kz.kcell.flow.files.Minio;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

@Service("generateDocumentSDR")
public class GenerateDocument implements JavaDelegate {

    private static final Map<String, String> initiatorsTitle =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("optimization", "Optimization Unit");
            map.put("transmission", "Transmission Unit");
            map.put("infrastructure", "Infrastructure Unit");
            map.put("operation", "Operation Unit");
            return Collections.unmodifiableMap(map);
        })).get();

    private static final Map<String, String> contractTypeTitle =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("rent", "Rent");
            map.put("power", "Power");
            map.put("rentAndPower", "Rent and Power counter (АП и электропитание по счетчику отдельными статьями)");
            map.put("rentWithPower", "Rent with Power (в АП включено электропитание)");
            return Collections.unmodifiableMap(map);
        })).get();

    private Minio minioClient;

    @Autowired
    public GenerateDocument(Minio minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String requestType = delegateExecution.getVariable("requestType").toString();

        POIFSFileSystem fs = new POIFSFileSystem(GenerateDocument.class.getResourceAsStream("/dismantleReplaceRequest/site_" + (requestType.equals("dismantle")?"dismantling":"replacement") + "_request.doc"));
        HWPFDocument doc = new HWPFDocument(fs);

        SimpleDateFormat dformat = new SimpleDateFormat("dd.MM.yyyy");
        Map<String, String> varsMap = new HashMap<>();

        boolean planningUnit = false;
        if(delegateExecution.hasVariable("planningUnit")){
            planningUnit = (boolean) delegateExecution.getVariable("planningUnit");
        }

        varsMap.put("$sitename", delegateExecution.getVariable("site_name").toString());
        varsMap.put("$requestNumber", delegateExecution.getBusinessKey());
        varsMap.put("$infilldate", dformat.format(new Date()));

        SpinJsonNode initiatorFull = delegateExecution.<JsonValue>getVariableTyped("initiatorFull").getValue();
        varsMap.put("$creator", initiatorFull.prop("firstName").toString().replaceAll("\"","") + " " + initiatorFull.prop("lastName").toString().replaceAll("\"",""));

        SpinJsonNode siteInformation = delegateExecution.<JsonValue>getVariableTyped("siteInformation").getValue();
        if (siteInformation.isArray()) {
            SpinList<SpinJsonNode> sites = siteInformation.elements();
            sites.forEach(s -> {
                String name = s.prop("name").stringValue();
                if("cell A:".equals(name)){
                    if(s.hasProp("gsmValue") && s.prop("gsmValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("gsmValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("gsmValue")))) {
                        varsMap.put("$gsmA", String.valueOf(s.prop("gsmValue").numberValue()));
                    } else {
                        varsMap.put("$gsmA", "");
                    }
                    if(s.hasProp("umtsValue") && s.prop("umtsValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("umtsValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("umtsValue")))) {
                        varsMap.put("$umtsA", String.valueOf(s.prop("umtsValue").numberValue()));
                    } else {
                        varsMap.put("$umtsA", "");
                    }
                    if(s.hasProp("lteValue") && s.prop("lteValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("lteValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("lteValue")))) {
                        varsMap.put("$lteA", String.valueOf(s.prop("lteValue").numberValue()));
                    } else {
                        varsMap.put("$lteA", "");
                    }
                } else if("cell B:".equals(name)){
                    if(s.hasProp("gsmValue") && s.prop("gsmValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("gsmValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("gsmValue")))) {
                        varsMap.put("$gsmB", String.valueOf(s.prop("gsmValue").numberValue()));
                    } else {
                        varsMap.put("$gsmB", "");
                    }
                    if(s.hasProp("umtsValue") && s.prop("umtsValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("umtsValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("umtsValue")))) {
                        varsMap.put("$umtsB", String.valueOf(s.prop("umtsValue").numberValue()));
                    } else {
                        varsMap.put("$umtsB", "");
                    }
                    if(s.hasProp("lteValue") && s.prop("lteValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("lteValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("lteValue")))) {
                        varsMap.put("$lteB", String.valueOf(s.prop("lteValue").numberValue()));
                    } else {
                        varsMap.put("$lteB", "");
                    }
                } else if("cell C:".equals(name)){
                    if(s.hasProp("gsmValue") && s.prop("gsmValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("gsmValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("gsmValue")))) {
                        varsMap.put("$gsmC", String.valueOf(s.prop("gsmValue").numberValue()));
                    } else {
                        varsMap.put("$gsmC", "");
                    }
                    if(s.hasProp("umtsValue") && s.prop("umtsValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("umtsValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("umtsValue")))) {
                        varsMap.put("$umtsC", String.valueOf(s.prop("umtsValue").numberValue()));
                    } else {
                        varsMap.put("$umtsC", "");
                    }
                    if(s.hasProp("lteValue") && s.prop("lteValue")!=null && !"null".equalsIgnoreCase(String.valueOf(s.prop("lteValue"))) && !"undefined".equalsIgnoreCase(String.valueOf(s.prop("lteValue")))) {
                        varsMap.put("$lteC", String.valueOf(s.prop("lteValue").numberValue()));
                    } else {
                        varsMap.put("$lteC", "");
                    }
                }
            });
        }

        if(requestType.equals("dismantle")){
            String dismantlingInitiator = delegateExecution.getVariable("dismantlingInitiator").toString();
            varsMap.put("$dismantlingInitiator", initiatorsTitle.get(dismantlingInitiator));
            varsMap.put("$dismantlingReason", delegateExecution.getVariable("dismantlingReason").toString());
            if(delegateExecution.hasVariable("project") && delegateExecution.getVariable("project")!=null){
                varsMap.put("$project", delegateExecution.getVariable("project").toString());
            } else {
                varsMap.put("$project", "");
            }
            SpinJsonNode alternativePlaces = delegateExecution.<JsonValue>getVariableTyped("alternativePlaces").getValue();
            if (alternativePlaces.isArray()) {
                SpinList<SpinJsonNode> alternatives = alternativePlaces.elements();
                int size = alternatives.size();
                int half = (size + 1) / 2;

                String firstHalf = "";
                String secondHalf = "";
                for(int i=0; i<size; i++){
                    if(i < half){
                        firstHalf = firstHalf + "Alternative " + (i + 1) + " " + alternatives.get(i).toString().replaceAll("\"","") + ((i < half-1) ? ", " : "");
                    } else if(i >= half){
                        secondHalf = secondHalf + "Alternative " + (i + 1) + " " + alternatives.get(i).toString().replaceAll("\"","") + ((i < size-1) ? ", " : "");
                    }
                }
                varsMap.put("$alternatives_firstPart", firstHalf);
                varsMap.put("$alternatives_secondPart", secondHalf);
            }

            varsMap.put("$rbsQuantity", String.valueOf(delegateExecution.getVariable("rbsQuantity")));
            SpinJsonNode rbsTypes = delegateExecution.<JsonValue>getVariableTyped("rbsTypes").getValue();
            if (rbsTypes.isArray()) {
                SpinList<SpinJsonNode> rbsList = rbsTypes.elements();
                int size = rbsList.size();
                String rbs = "";
                for(int i=0; i<size; i++){
                    rbs = rbs + rbsList.get(i).toString().replaceAll("\"","") + ((i < size-1) ? ", " : "");
                }
                varsMap.put("$rbsType", rbs);
            }

            varsMap.put("$band", delegateExecution.getVariable("band").toString());
            varsMap.put("$squareMeter", String.valueOf(delegateExecution.getVariable("squareMeter")));

            String rbsLocation = delegateExecution.getVariable("rbsLocation").toString();
            if("Other".equals(rbsLocation)){
                varsMap.put("$rbsLocation", "other " + delegateExecution.getVariable("otherRbsLocation").toString());
            } else {
                varsMap.put("$rbsLocation", rbsLocation);
            }

            SpinJsonNode gsmAntennaTypes = delegateExecution.<JsonValue>getVariableTyped("gsmAntennaTypes").getValue();
            if (gsmAntennaTypes.isArray()) {
                SpinList<SpinJsonNode> gsmAntennas = gsmAntennaTypes.elements();
                int size = gsmAntennas.size();
                String gsmAntenna = "";
                for(int i=0; i<size; i++){
                    gsmAntenna = gsmAntenna + gsmAntennas.get(i).toString().replaceAll("\"","") + ((i < size-1) ? ", " : "");
                }
                varsMap.put("$gAT", gsmAntenna);
            }

            varsMap.put("$transAntennaType", delegateExecution.getVariable("transmissionAntennaType").toString());
            varsMap.put("$contractId", delegateExecution.getVariable("contractId").toString());
            varsMap.put("$contractType", contractTypeTitle.get(delegateExecution.getVariable("contractType").toString()));
            varsMap.put("$legallyName", delegateExecution.getVariable("legallyName").toString());
            varsMap.put("$address", delegateExecution.getVariable("address").toString());
            varsMap.put("$contactInformation", delegateExecution.getVariable("contactInformation").toString());
        } else {
            String replacementInitiator = delegateExecution.getVariable("replacementInitiator").toString();
            varsMap.put("$replacementInitiator", initiatorsTitle.get(replacementInitiator));
            varsMap.put("$replacementReason", delegateExecution.getVariable("replacementReason").toString());
            varsMap.put("$siteToName", delegateExecution.getVariable("site_to_name").toString());
            varsMap.put("$rbsFromQuantity", String.valueOf(delegateExecution.getVariable("siteFromRbsQuantity")));
            varsMap.put("$rbsToQuantity", String.valueOf(delegateExecution.getVariable("siteToRbsQuantity")));
            varsMap.put("$siteFromLatitude", String.valueOf(delegateExecution.getVariable("siteFromLatitude")));
            varsMap.put("$siteToLatitude", String.valueOf(delegateExecution.getVariable("siteToLatitude")));
            varsMap.put("$siteFromLongitude", String.valueOf(delegateExecution.getVariable("siteFromLongitude")));
            varsMap.put("$siteToLongitude", String.valueOf(delegateExecution.getVariable("siteToLongitude")));

            SpinJsonNode siteFromRbsTypes = delegateExecution.<JsonValue>getVariableTyped("siteFromRbsTypes").getValue();
            if (siteFromRbsTypes.isArray()) {
                SpinList<SpinJsonNode> rbsList = siteFromRbsTypes.elements();
                int size = rbsList.size();
                String rbs = "";
                for(int i=0; i<size; i++){
                    rbs = rbs + rbsList.get(i).toString().replaceAll("\"","") + ((i < size-1) ? ", " : "");
                }
                varsMap.put("$siteFromRbsType", rbs);
            }

            SpinJsonNode siteToRbsTypes = delegateExecution.<JsonValue>getVariableTyped("siteToRbsTypes").getValue();
            if (siteToRbsTypes.isArray()) {
                SpinList<SpinJsonNode> rbsList = siteToRbsTypes.elements();
                int size = rbsList.size();
                String rbs = "";
                for(int i=0; i<size; i++){
                    rbs = rbs + rbsList.get(i).toString().replaceAll("\"","") + ((i < size-1) ? ", " : "");
                }
                varsMap.put("$siteToRbsType", rbs);
            }
            varsMap.put("$siteFromBand", String.valueOf(delegateExecution.getVariable("siteFromBand")));
            varsMap.put("$siteToBand", String.valueOf(delegateExecution.getVariable("siteToBand")));
            varsMap.put("$siteFromRbsLoc", String.valueOf(delegateExecution.getVariable("siteFromRbsLocation")));
            varsMap.put("$siteToRbsLoc", String.valueOf(delegateExecution.getVariable("siteToRbsLocation")));

            if(delegateExecution.hasVariable("siteFromSquareMeter") && delegateExecution.getVariable("siteFromSquareMeter")!=null){
                varsMap.put("$siteFromSquareM", delegateExecution.getVariable("siteFromSquareMeter").toString());
            } else {
                varsMap.put("$siteFromSquareM", "");
            }
            if(delegateExecution.hasVariable("siteToSquareMeter") && delegateExecution.getVariable("siteToSquareMeter")!=null){
                varsMap.put("$siteToSquareM", delegateExecution.getVariable("siteToSquareMeter").toString());
            } else {
                varsMap.put("$siteToSquareM", "");
            }

            SpinJsonNode siteFromGsmAntennaTypes = delegateExecution.<JsonValue>getVariableTyped("siteFromGsmAntennaTypes").getValue();
            if (siteFromGsmAntennaTypes.isArray()) {
                SpinList<SpinJsonNode> gsmAntennas = siteFromGsmAntennaTypes.elements();
                int size = gsmAntennas.size();
                String gsmAntenna = "";
                for(int i=0; i<size; i++){
                    gsmAntenna = gsmAntenna + gsmAntennas.get(i).toString().replaceAll("\"","") + ((i < size-1) ? ", " : "");
                }
                varsMap.put("$siteFromGsmAnt", gsmAntenna);
            }
            SpinJsonNode siteToGsmAntennaTypes = delegateExecution.<JsonValue>getVariableTyped("siteToGsmAntennaTypes").getValue();
            if (siteToGsmAntennaTypes.isArray()) {
                SpinList<SpinJsonNode> gsmAntennas = siteToGsmAntennaTypes.elements();
                int size = gsmAntennas.size();
                String gsmAntenna = "";
                for(int i=0; i<size; i++){
                    gsmAntenna = gsmAntenna + gsmAntennas.get(i).toString().replaceAll("\"","") + ((i < size-1) ? ", " : "");
                }
                varsMap.put("$siteToGsmAnt", gsmAntenna);
            }

            varsMap.put("$siteFromTransAnt", String.valueOf(delegateExecution.getVariable("siteFromTransmissionAntennaType")));
            varsMap.put("$siteToTransAnt", String.valueOf(delegateExecution.getVariable("siteToTransmissionAntennaType")));
            varsMap.put("$siteFromContractId", String.valueOf(delegateExecution.getVariable("siteFromContractId")));
            if(delegateExecution.hasVariable("siteToContractId") && delegateExecution.getVariable("siteToContractId")!=null){
                varsMap.put("$siteToContractId", delegateExecution.getVariable("siteToContractId").toString());
            } else {
                varsMap.put("$siteToContractId", "");
            }

            if(delegateExecution.hasVariable("siteFromContractType") && delegateExecution.getVariable("siteFromContractType")!=null && !delegateExecution.getVariable("siteFromContractType").toString().equals("")){
                varsMap.put("$siteFromContractType", contractTypeTitle.get(delegateExecution.getVariable("siteFromContractType").toString()));
            } else {
                varsMap.put("$siteFromContractType", "");
            }
            if(delegateExecution.hasVariable("siteToContractType") && delegateExecution.getVariable("siteToContractType")!=null && !delegateExecution.getVariable("siteToContractType").toString().equals("")){
                varsMap.put("$siteToContractType", contractTypeTitle.get(delegateExecution.getVariable("siteToContractType").toString()));
            } else {
                varsMap.put("$siteToContractType", "");
            }

            if(delegateExecution.hasVariable("siteFromLegallyName") && delegateExecution.getVariable("siteFromLegallyName")!=null){
                varsMap.put("$siteFromLegallyName", delegateExecution.getVariable("siteFromLegallyName").toString());
            } else {
                varsMap.put("$siteFromLegallyName", "");
            }
            if(delegateExecution.hasVariable("siteToLegallyName") && delegateExecution.getVariable("siteToLegallyName")!=null){
                varsMap.put("$siteToLegallyName", delegateExecution.getVariable("siteToLegallyName").toString());
            } else {
                varsMap.put("$siteToLegallyName", "");
            }

            if(delegateExecution.hasVariable("siteFromAddress") && delegateExecution.getVariable("siteFromAddress")!=null){
                varsMap.put("$siteFromAddress", String.valueOf(delegateExecution.getVariable("siteFromAddress")));
            } else {
                varsMap.put("$siteFromAddress", "");
            }
            if(delegateExecution.hasVariable("siteToAddress") && delegateExecution.getVariable("siteToAddress")!=null){
                varsMap.put("$siteToAddress", String.valueOf(delegateExecution.getVariable("siteToAddress")));
            } else {
                varsMap.put("$siteToAddress", "");
            }

            varsMap.put("$siteFromContactInfo", String.valueOf(delegateExecution.getVariable("siteFromContactInformation")));
            varsMap.put("$siteToContactInfo", String.valueOf(delegateExecution.getVariable("siteToContactInformation")));
        }

        if(delegateExecution.hasVariable("startComment") && delegateExecution.getVariable("startComment")!=null){
            varsMap.put("$comments", delegateExecution.getVariable("startComment").toString());
        } else {
            varsMap.put("$comments", "");
        }

        SpinJsonNode resolutions = delegateExecution.<JsonValue>getVariableTyped("resolutions").getValue();
        if (resolutions.isArray()) {
            SpinList<SpinJsonNode> resolution = resolutions.elements();
            resolution.forEach(r -> {
                if (r.hasProp("taskDefinitionKey")) {
                    String taskKey = r.prop("taskDefinitionKey").stringValue();
                    String taskName = r.prop("taskName").stringValue();
                    String result = r.prop("resolution").stringValue();
                    String taskEndDate = r.prop("taskEndDate").stringValue().substring(0, 10);
                    String assigneeName = r.prop("assigneeName").stringValue();

                    if ("region_approve".equals(taskKey)) {
                        if ("approve".equals(result)) {
                            varsMap.put(requestType.equals("dismantle")?"$head_y":"$1y", "√");
                            varsMap.put(requestType.equals("dismantle")?"$head_n":"$1n", "");
                        } else {
                            varsMap.put(requestType.equals("dismantle")?"$head_y":"$1y", "");
                            varsMap.put(requestType.equals("dismantle")?"$head_n":"$1n", "√");
                        }
                        varsMap.put(requestType.equals("dismantle")?"$head_f":"$1f", assigneeName);
                        varsMap.put(requestType.equals("dismantle")?"$head_date":"$1d", taskEndDate);
                    } else if("planning_group".equals(taskKey)){
                        if ("approve".equals(result)) {
                            varsMap.put(requestType.equals("dismantle")?"$2y":"$3y", "√");
                            varsMap.put(requestType.equals("dismantle")?"$2n":"$3n", "");
                        } else {
                            varsMap.put(requestType.equals("dismantle")?"$2y":"$3y", "");
                            varsMap.put(requestType.equals("dismantle")?"$2n":"$3n", "√");
                        }
                        varsMap.put(requestType.equals("dismantle")?"$2d":"$3d", taskEndDate);
                        varsMap.put(requestType.equals("dismantle")?"$2f":"$3f", assigneeName);
                        varsMap.put("$2y", "");
                        varsMap.put("$2n", "");
                        varsMap.put("$2d", "");
                        varsMap.put("$2f", "");
                        varsMap.put("$4y", "");
                        varsMap.put("$4n", "");
                        varsMap.put("$4d", "");
                        varsMap.put("$4f", "");
                        varsMap.put("$5y", "");
                        varsMap.put("$5n", "");
                        varsMap.put("$5d", "");
                        varsMap.put("$5f", "");
                        varsMap.put("$6y", "");
                        varsMap.put("$6n", "");
                        varsMap.put("$6d", "");
                        varsMap.put("$6f", "");
                    } else {
                        if ("central group \"Central Planning Unit\"".equals(taskName)) {
                            if ("approve".equals(result)) {
                                varsMap.put(requestType.equals("dismantle")?"$2y":"$3y", "√");
                                varsMap.put(requestType.equals("dismantle")?"$2n":"$3n", "");
                            } else {
                                varsMap.put(requestType.equals("dismantle")?"$2y":"$3y", "");
                                varsMap.put(requestType.equals("dismantle")?"$2n":"$3n", "√");
                            }
                            varsMap.put(requestType.equals("dismantle")?"$2d":"$3d", taskEndDate);
                            varsMap.put(requestType.equals("dismantle")?"$2f":"$3f", assigneeName);
                        } else if ("central group \"Central Transmission Unit\"".equals(taskName)) {
                            if ("approve".equals(result)) {
                                varsMap.put(requestType.equals("dismantle")?"$ctu_y":"$4y", "√");
                                varsMap.put(requestType.equals("dismantle")?"$ctu_n":"$4n", "");
                            } else {
                                varsMap.put(requestType.equals("dismantle")?"$ctu_y":"$4y", "");
                                varsMap.put(requestType.equals("dismantle")?"$ctu_n":"$4n", "√");
                            }
                            varsMap.put(requestType.equals("dismantle")?"$ctu_date":"$4d", taskEndDate);
                            varsMap.put("$4f", assigneeName);
                        } else if ("central group \"Central S&FM Unit\"".equals(taskName)) {
                            if ("approve".equals(result)) {
                                varsMap.put(requestType.equals("dismantle")?"$csfu_y":"$5y", "√");
                                varsMap.put(requestType.equals("dismantle")?"$csfu_n":"$5n", "");
                            } else {
                                varsMap.put(requestType.equals("dismantle")?"$csfu_y":"$5y", "");
                                varsMap.put(requestType.equals("dismantle")?"$csfu_n":"$5n", "√");
                            }
                            varsMap.put(requestType.equals("dismantle")?"$csfu_date":"$5d", taskEndDate);
                            varsMap.put("$5f", assigneeName);
                        } else if ("central group \"Central Leasing Unit\"".equals(taskName)) {
                            if ("approve".equals(result)) {
                                varsMap.put(requestType.equals("dismantle")?"$1y":"$2y", "√");
                                varsMap.put(requestType.equals("dismantle")?"$1n":"$2n", "");
                            } else {
                                varsMap.put(requestType.equals("dismantle")?"$1y":"$2y", "");
                                varsMap.put(requestType.equals("dismantle")?"$1n":"$2n", "√");
                            }
                            varsMap.put(requestType.equals("dismantle")?"$1d":"$2d", taskEndDate);
                            varsMap.put(requestType.equals("dismantle")?"$1f":"$2f", assigneeName);
                        } else if ("central group \"Central SAO Unit\"".equals(taskName)) {
                            if ("approve".equals(result)) {
                                varsMap.put(requestType.equals("dismantle")?"$3y":"$6y", "√");
                                varsMap.put(requestType.equals("dismantle")?"$3n":"$6n", "");
                            } else {
                                varsMap.put(requestType.equals("dismantle")?"$3y":"$6y", "");
                                varsMap.put(requestType.equals("dismantle")?"$3n":"$6n", "√");
                            }
                            varsMap.put(requestType.equals("dismantle")?"$3d":"$6d", taskEndDate);
                            varsMap.put(requestType.equals("dismantle")?"$3f":"$6f", assigneeName);
                        }
                    }
                }
            });

            if ("region_approve".equals(resolution.get(resolution.size() - 1).prop("taskDefinitionKey").stringValue())) {
                varsMap.put("$2y", "");
                varsMap.put("$2n", "");
                varsMap.put("$2d", "");
                varsMap.put("$2f", "");
                varsMap.put("$3y", "");
                varsMap.put("$3n", "");
                varsMap.put("$3d", "");
                varsMap.put("$3f", "");
                if(requestType.equals("dismantle")){
                    varsMap.put("$1y", "");
                    varsMap.put("$1n", "");
                    varsMap.put("$1d", "");
                    varsMap.put("$1f", "");
                    varsMap.put("$ctu_y", "");
                    varsMap.put("$ctu_n", "");
                    varsMap.put("$ctu_date", "");
                    varsMap.put("$4f", "");
                    varsMap.put("$csfu_y", "");
                    varsMap.put("$csfu_n", "");
                    varsMap.put("$csfu_date", "");
                    varsMap.put("$5f", "");
                } else {
                    varsMap.put("$4y", "");
                    varsMap.put("$4n", "");
                    varsMap.put("$4d", "");
                    varsMap.put("$4f", "");
                    varsMap.put("$5y", "");
                    varsMap.put("$5n", "");
                    varsMap.put("$5d", "");
                    varsMap.put("$5f", "");
                    varsMap.put("$6y", "");
                    varsMap.put("$6n", "");
                    varsMap.put("$6d", "");
                    varsMap.put("$6f", "");
                }
            }
        }

        for (Map.Entry<String, String> entry : varsMap.entrySet()) {
            doc = replaceText(doc, entry.getKey(), entry.getValue());
        }

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        doc.write(b); // doc should be a XWPFDocument
        InputStream inputStream = new ByteArrayInputStream(b.toByteArray());

        String name = requestType.equals("dismantle") ? "Site Dismantling Request.doc" : "Site replacement Request.doc";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;

        minioClient.saveFile(path, inputStream, "application/msword");

        String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable(requestType.equals("dismantle") ? "siteDismantlingDocument" : "siteReplacementDocument", SpinValues.jsonValue(json));

        fs.close();
        doc.close();
        b.close();
        inputStream.close();
    }

    private static HWPFDocument replaceText(HWPFDocument doc, String findText, String replaceText){
        Range r1 = doc.getRange();
        for (int i = 0; i < r1.numSections(); ++i ) {
            Section s = r1.getSection(i);
            for (int x = 0; x < s.numParagraphs(); x++) {
                Paragraph p = s.getParagraph(x);
                for (int z = 0; z < p.numCharacterRuns(); z++) {
                    CharacterRun run = p.getCharacterRun(z);
                    String text = run.text();
                    if(text.contains(findText)) {
                        run.replaceText(findText, replaceText);
                    }
                }
            }
        }
        return doc;
    }

}
