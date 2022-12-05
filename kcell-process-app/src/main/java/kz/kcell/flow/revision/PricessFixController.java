
package kz.kcell.flow.revision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.kcell.bpm.SetPricesDelegate;
import kz.kcell.bpm.revision.SetWorkVariables;
import kz.kcell.flow.repository.custom.ReportRepository;
import kz.kcell.flow.revision.JrBlankFixService;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping("/pricesfix")
@Log
public class PricessFixController {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RuntimeService runtimeService;

    @Value("${mail.message.baseurl:http://localhost}")
    String baseUri;

    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> execute() throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        List<String> keys = new ArrayList<>();
        keys.add("W-");
        keys.add("S-");
        keys.add("E-");
        keys.add("Ast-");
        keys.add("Alm-");
        keys.add("N&C-");

        for (String key : keys) {
            List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().processDefinitionKey("Revision").processInstanceBusinessKeyLike(key + '%').list();
            log.info("Checking " + key + " list size: " + list.size());

            for (ProcessInstance p : list) {
                try {
                    String mainContract = runtimeService.getVariable(p.getProcessInstanceId(), "mainContract").toString();
                    Object prices = runtimeService.getVariable(p.getProcessInstanceId(), "workPrices");

                    if(prices==null){
                        log.info("setting price to " + p.getBusinessKey());
                        log.info("----------------------------------------------------------");

                        ObjectMapper mapper = new ObjectMapper();

                        ArrayNode worksPriceList = mapper.createArrayNode();
                        Map<String, String> uniqueWorks = new HashMap<>();
                        Map<String, String> worksTitleMap = new HashMap<>();

                        if ("Revision".equals(mainContract) || "Roll-out".equals(mainContract)) {

                        } else {
                            Map<String, JsonNode> worksPriceMap = new HashMap<>();

                            String siteRegion = (String) runtimeService.getVariable(p.getProcessInstanceId(),"siteRegion");
                            if("nc".equals(siteRegion) || "east".equals(siteRegion)){
                                siteRegion = "astana";
                            }

                            InputStream fis = SetWorkVariables.class.getResourceAsStream("/revision/newWorkPrice.json");
                            InputStreamReader reader = new InputStreamReader(fis, "utf-8");
                            ArrayNode json = (ArrayNode) mapper.readTree(reader);

                            for (JsonNode workPrice : json) {
                                workPrice.get("price").toString();
                                worksPriceMap.put(workPrice.get("id").textValue(), workPrice.get("price"));
                                worksTitleMap.put(workPrice.get("id").textValue(), workPrice.get("title").textValue());
                            }

                            ArrayNode workPrices = mapper.createArrayNode();
                            ArrayNode jobWorks = (ArrayNode) mapper.readTree(runtimeService.getVariable(p.getProcessInstanceId(),"jobWorks").toString());
                            BigDecimal jobWorksTotal = BigDecimal.ZERO;
                            for (JsonNode work : jobWorks) {
                                ObjectNode workPrice = work.deepCopy();
                                if (workPrice.get("relatedSites") == null) {
                                    workPrice.set("relatedSites", mapper.createArrayNode());
                                }

                                if (!uniqueWorks.containsKey(work.get("sapServiceNumber").textValue())) {
                                    JsonNode priceJson = worksPriceMap.get(work.get("sapServiceNumber").textValue());
                                    String title = worksTitleMap.get(work.get("sapServiceNumber").textValue());

                                    ObjectNode workPriceJson = mapper.createObjectNode();
                                    workPriceJson.put("sapServiceNumber", work.get("sapServiceNumber").textValue());
                                    workPriceJson.put("priceWithMaterial", priceJson.get(siteRegion).get("with_material").textValue());
                                    workPriceJson.put("priceWithoutMaterial", priceJson.get(siteRegion).get("without_material").textValue());
                                    workPriceJson.put("price", priceJson.get(siteRegion).get(work.has("materialsProvidedBy") && "subcontractor".equals(work.get("materialsProvidedBy").textValue()) ? "with_material" : "without_material").textValue());
                                    workPriceJson.put("title", title);
                                    worksPriceList.add(workPriceJson);
                                    uniqueWorks.put(work.get("sapServiceNumber").textValue(), "");
                                }

                                JsonNode priceJson = worksPriceMap.get(work.get("sapServiceNumber").textValue());
                                BigDecimal unitWorkPrice = new BigDecimal(priceJson.get(siteRegion).get(work.has("materialsProvidedBy") && "subcontractor".equals(work.get("materialsProvidedBy").textValue()) ? "with_material" : "without_material").textValue());
                                BigDecimal unitTransportationPrice = unitWorkPrice.multiply(new BigDecimal("0.00"));
                                unitTransportationPrice = unitTransportationPrice.setScale(2, RoundingMode.DOWN);

                                BigDecimal unitWorkPricePlusTx = unitWorkPrice.add(unitTransportationPrice);

                                if (workPrice.get("relatedSites").size() > 0) {
                                    BigDecimal unitWorkPricePerSite = unitWorkPricePlusTx.divide(new BigDecimal(workPrice.get("relatedSites").size()), 2, RoundingMode.DOWN);
                                    BigDecimal netWorkPricePerSite = unitWorkPricePerSite.multiply(new BigDecimal(workPrice.get("quantity").asText()));
                                    workPrice.put("unitWorkPricePerSite", unitWorkPricePerSite.setScale(2, RoundingMode.DOWN).toString());
                                    workPrice.put("netWorkPricePerSite", netWorkPricePerSite.setScale(2, RoundingMode.DOWN).toString());
                                } else {
                                    BigDecimal unitWorkPricePerSite = unitWorkPricePlusTx.divide(new BigDecimal(1), 2, RoundingMode.DOWN);
                                    BigDecimal netWorkPricePerSite = unitWorkPricePerSite.multiply(new BigDecimal(workPrice.get("quantity").asText()));
                                    workPrice.put("unitWorkPricePerSite", unitWorkPricePerSite.setScale(2, RoundingMode.DOWN).toString());
                                    workPrice.put("netWorkPricePerSite", netWorkPricePerSite.setScale(2, RoundingMode.DOWN).toString());
                                }

                                BigDecimal basePriceByQuantity = unitWorkPrice.multiply(new BigDecimal(workPrice.get("quantity").asText()));

                                BigDecimal total = unitWorkPricePlusTx.multiply(new BigDecimal(workPrice.get("quantity").asText()));
                                workPrice.put("unitWorkPrice", unitWorkPrice.setScale(2, RoundingMode.DOWN).toString());
                                workPrice.put("unitWorkPricePlusTx", unitWorkPricePlusTx.setScale(2, RoundingMode.DOWN).toString());
                                workPrice.put("basePriceByQuantity", basePriceByQuantity.setScale(2, RoundingMode.DOWN).toString());
                                workPrice.put("total", total.setScale(2, RoundingMode.DOWN).toString());

                                jobWorksTotal = jobWorksTotal.add(total);

                                workPrice.put("basePrice", worksPriceMap.get(work.get("sapServiceNumber").asText()).get(siteRegion).get("without_material").textValue());
                                workPrice.remove("$$hashKey");
                                workPrices.add(workPrice);
                            }
                            runtimeService.setVariable(p.getProcessInstanceId(),"workPrices", SpinValues.jsonValue(workPrices.toString()));
                            runtimeService.setVariable(p.getProcessInstanceId(),"worksPriceList", SpinValues.jsonValue(worksPriceList.toString()).create());
                            runtimeService.setVariable(p.getProcessInstanceId(),"jobWorksTotal", jobWorksTotal.setScale(2, RoundingMode.DOWN).toString());
                        }
                    }
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        return ResponseEntity.ok("ok");
    }

    @RequestMapping(value = "/fix", method = RequestMethod.GET)
    @ResponseBody
    public void fix() throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return;
        }

        List<String> keys = new ArrayList<>();
        keys.add("W-");
        keys.add("S-");
        keys.add("E-");
        keys.add("Ast-");
        keys.add("Alm-");
        keys.add("N&C-");

        ObjectMapper mapper = new ObjectMapper();
        InputStream fis = SetWorkVariables.class.getResourceAsStream("/revision/newWorkPrice.json");
        InputStreamReader reader = new InputStreamReader(fis, "utf-8");
        ArrayNode json = (ArrayNode) mapper.readTree(reader);

        for (String key : keys) {
            List<ProcessInstance> list = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey("Revision")
                .processInstanceBusinessKeyLike(key + '%')
                .variableValueEquals("mainContract", "Roll-outRevision2020")
                .list();

            log.info("Checking " + key + " list size: " + list.size());

            for (ProcessInstance p : list) {

                Map<String, JsonNode> worksPriceMap = new HashMap<>();
                Map<String, String> workProvidedBy = new HashMap<>();

                for (JsonNode workPrice : json) {
                    workPrice.get("price").toString();
                    worksPriceMap.put(workPrice.get("id").textValue(), workPrice.get("price"));
                }

                try {
                    SpinJsonNode workPrices = runtimeService.<JsonValue>getVariableTyped(p.getProcessInstanceId(), "workPrices").getValue();
                    SpinJsonNode jobWorks = runtimeService.<JsonValue>getVariableTyped(p.getProcessInstanceId(), "jobWorks").getValue();

                    if (workPrices.isArray()) {
                        SpinList<SpinJsonNode> workPricesList = workPrices.elements();
                        workPricesList.forEach(file -> {
                            if (file.hasProp("materialsProvidedBy") && "true".equals(file.prop("materialsProvidedBy").stringValue())) {
                                JsonNode priceJson = worksPriceMap.get(file.prop("sapServiceNumber").stringValue());

                                String siteRegion = (String) runtimeService.getVariable(p.getProcessInstanceId(),"siteRegion");
                                if("nc".equals(siteRegion) || "east".equals(siteRegion)){
                                    siteRegion = "astana";
                                }
                                if(priceJson.get(siteRegion).get("without_material").textValue().equals(file.prop("basePrice").stringValue())){
                                    file.prop("materialsProvidedBy", "kcell");
                                } else {
                                    file.prop("materialsProvidedBy", "subcontractor");
                                }
                                workProvidedBy.put(file.prop("sapServiceNumber").stringValue(), file.prop("materialsProvidedBy").stringValue());
                                log.info("Pricess changed process: " + p.getBusinessKey() + " work sap number: " + file.prop("sapServiceNumber").stringValue() + " materialsProvidedBy: " + file.prop("materialsProvidedBy"));
                            }
                        });
                        log.info(workPricesList.toString());
                        runtimeService.setVariable(p.getProcessInstanceId(), "workPrices", SpinJsonNode.JSON(workPricesList.toString()));
                    }

                    if (jobWorks.isArray()) {
                        SpinList<SpinJsonNode> jobWorksList = jobWorks.elements();
                        jobWorksList.forEach(file -> {
                            if (file.hasProp("materialsProvidedBy") && "true".equals(file.prop("materialsProvidedBy").stringValue())) {
                                file.prop("materialsProvidedBy", workProvidedBy.get(file.prop("sapServiceNumber").stringValue()));
                                log.info("works changed process: " + p.getBusinessKey() + " work sap number: " + file.prop("sapServiceNumber").stringValue() + " materialsProvidedBy: " + file.prop("materialsProvidedBy"));
                            }
                        });
                        log.info(jobWorksList.toString());
                        runtimeService.setVariable(p.getProcessInstanceId(), "jobWorks", SpinJsonNode.JSON(jobWorksList.toString()));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                log.info("=============================================");
            }
        }

        log.info("finished! ");
    }
}
