package kz.kcell.flow.dismantleReplace;

import kz.kcell.flow.files.Minio;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
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
            map.put("planningUnit", "Planning unit");
            map.put("siteAcquisionUnit", "Site Acquisition unit");
            map.put("implementationUnit", "Implementation Unit");
            map.put("other", "Other");
            return Collections.unmodifiableMap(map);
        })).get();

    private static final Map<String, String> contractTypeTitle =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("rent", "Rent");
            map.put("power", "Powerit");
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

        POIFSFileSystem fs = new POIFSFileSystem(GenerateDocument.class.getResourceAsStream("/dismantleReplaceRequest/site_dismantling_request.doc"));
        HWPFDocument doc = new HWPFDocument(fs);

        SimpleDateFormat dformat = new SimpleDateFormat("dd.MM.yyyy");
        Map<String, String> varsMap = new HashMap<>();

        varsMap.put("$sitename", delegateExecution.getVariable("site_name").toString());
        varsMap.put("$requestNumber", delegateExecution.getBusinessKey());
        varsMap.put("$infilldate", dformat.format(new Date()));

        SpinJsonNode initiatorFull = delegateExecution.<JsonValue>getVariableTyped("initiatorFull").getValue();
        varsMap.put("$creator", initiatorFull.prop("firstName").toString().replaceAll("\"","") + " " + initiatorFull.prop("lastName").toString().replaceAll("\"",""));

        String dismantlingInitiator = delegateExecution.getVariable("dismantlingInitiator").toString();
        if("other".equals(dismantlingInitiator)){
            varsMap.put("$dismantlingInitiator", delegateExecution.getVariable("otherInitiator").toString());
        } else {
            varsMap.put("$dismantlingInitiator", initiatorsTitle.get(dismantlingInitiator));
        }

        varsMap.put("$dismantlingReason", delegateExecution.getVariable("dismantlingReason").toString());

        SpinJsonNode siteInformation = delegateExecution.<JsonValue>getVariableTyped("siteInformation").getValue();
        if (siteInformation.isArray()) {
            SpinList<SpinJsonNode> sites = siteInformation.elements();
            sites.forEach(s -> {
                String name = s.prop("name").stringValue();
                if("cell A:".equals(name)){
                    if(s.hasProp("gsmValue") && s.prop("gsmValue")!=null && !"undefined".equals(s.prop("gsmValue"))) {
                        varsMap.put("$gsmA", String.valueOf(s.prop("gsmValue").numberValue()));
                    } else {
                        varsMap.put("$gsmA", "");
                    }
                    if(s.hasProp("umtsValue") && s.prop("umtsValue")!=null && !"undefined".equals(s.prop("umtsValue"))) {
                        varsMap.put("$umtsA", String.valueOf(s.prop("umtsValue").numberValue()));
                    } else {
                        varsMap.put("$umtsA", "");
                    }
                    if(s.hasProp("lteValue") && s.prop("lteValue")!=null && !"undefined".equals(s.prop("lteValue"))) {
                        varsMap.put("$lteA", String.valueOf(s.prop("lteValue").numberValue()));
                    } else {
                        varsMap.put("$lteA", "");
                    }
                } else if("cell B:".equals(name)){
                    if(s.hasProp("gsmValue") && s.prop("gsmValue")!=null && !"undefined".equals(s.prop("gsmValue"))) {
                        varsMap.put("$gsmB", String.valueOf(s.prop("gsmValue").numberValue()));
                    } else {
                        varsMap.put("$gsmB", "");
                    }
                    if(s.hasProp("umtsValue") && s.prop("umtsValue")!=null && !"undefined".equals(s.prop("umtsValue"))) {
                        varsMap.put("$umtsB", String.valueOf(s.prop("umtsValue").numberValue()));
                    } else {
                        varsMap.put("$umtsB", "");
                    }
                    if(s.hasProp("lteValue") && s.prop("lteValue")!=null && !"undefined".equals(s.prop("lteValue"))) {
                        varsMap.put("$lteB", String.valueOf(s.prop("lteValue").numberValue()));
                    } else {
                        varsMap.put("$lteB", "");
                    }
                } else if("cell C:".equals(name)){
                    if(s.hasProp("gsmValue") && s.prop("gsmValue")!=null && !"undefined".equals(s.prop("gsmValue"))) {
                        varsMap.put("$gsmC", String.valueOf(s.prop("gsmValue").numberValue()));
                    } else {
                        varsMap.put("$gsmC", "");
                    }
                    if(s.hasProp("umtsValue") && s.prop("umtsValue")!=null && !"undefined".equals(s.prop("umtsValue"))) {
                        varsMap.put("$umtsC", String.valueOf(s.prop("umtsValue").numberValue()));
                    } else {
                        varsMap.put("$umtsC", "");
                    }
                    if(s.hasProp("lteValue") && s.prop("lteValue")!=null && !"undefined".equals(s.prop("lteValue"))) {
                        varsMap.put("$lteC", String.valueOf(s.prop("lteValue").numberValue()));
                    } else {
                        varsMap.put("$lteC", "");
                    }
                }
            });
        }

        if(delegateExecution.hasVariable("project")){
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
        varsMap.put("$rbsType", delegateExecution.getVariable("rbsType").toString());
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

        if(delegateExecution.hasVariable("startComment")){
            varsMap.put("$comments", delegateExecution.getVariable("startComment").toString());
        } else {
            varsMap.put("$comments", "");
        }

        SpinJsonNode resolutions = delegateExecution.<JsonValue>getVariableTyped("resolutions").getValue();
        if (resolutions.isArray()) {
            SpinList<SpinJsonNode> resolution = resolutions.elements();
            resolution.forEach(r -> {
                if(r.hasProp("taskDefinitionKey")){
                    String taskKey = r.prop("taskDefinitionKey").stringValue();
                    String taskName = r.prop("taskName").stringValue();
                    String result = r.prop("resolution").stringValue();
                    String taskEndDate = r.prop("taskEndDate").stringValue().substring(0,10);

                    if("region_approve".equals(taskKey)){
                        if("approve".equals(result)){
                            varsMap.put("$head_y", "v");
                            varsMap.put("$head_n", "");
                        } else {
                            varsMap.put("$head_y", "");
                            varsMap.put("$head_n", "x");
                        }
                        varsMap.put("$head_date", taskEndDate);
                    } else {
                        if("central group \"Central Planning Unit\"".equals(taskName)){
                            if("approve".equals(result)){
                                varsMap.put("$2y", "v");
                                varsMap.put("$2n", "");
                            } else {
                                varsMap.put("$2y", "");
                                varsMap.put("$2n", "x");
                            }
                            varsMap.put("$2d", taskEndDate);
                        } else if("central group \"Central Transmission Unit\"".equals(taskName)){
                            if("approve".equals(result)){
                                varsMap.put("$ctu_y", "v");
                                varsMap.put("$ctu_n", "");
                            } else {
                                varsMap.put("$ctu_y", "");
                                varsMap.put("$ctu_n", "x");
                            }
                            varsMap.put("$ctu_date", taskEndDate);
                        } else if("central group \"Central S&FM Unit\"".equals(taskName)){
                            if("approve".equals(result)){
                                varsMap.put("$csfu_y", "v");
                                varsMap.put("$csfu_n", "");
                            } else {
                                varsMap.put("$csfu_y", "");
                                varsMap.put("$csfu_n", "x");
                            }
                            varsMap.put("$csfu_date", taskEndDate);
                        } else if("central group \"Central SAO Unit\"".equals(taskName)){
                            if("approve".equals(result)){
                                varsMap.put("$1y", "v");
                                varsMap.put("$1n", "");
                            } else {
                                varsMap.put("$1y", "");
                                varsMap.put("$1n", "x");
                            }
                            varsMap.put("$1d", taskEndDate);
                        }
                    }
                }
            });
        }

        for (Map.Entry<String, String> entry : varsMap.entrySet()) {
            doc = replaceText(doc, entry.getKey(), entry.getValue());
        }

        ByteArrayOutputStream b = new ByteArrayOutputStream();
        doc.write(b); // doc should be a XWPFDocument
        InputStream inputStream = new ByteArrayInputStream(b.toByteArray());

        String name = "Site Dismantling Request.doc";
        String path = delegateExecution.getProcessInstanceId() + "/" + name;

        minioClient.saveFile(path, inputStream, "application/msword");

        String json = "{\"name\" : \"" + name + "\",\"path\" : \"" + path + "\"}";
        delegateExecution.setVariable("siteDismantlingDocument", SpinValues.jsonValue(json));
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
