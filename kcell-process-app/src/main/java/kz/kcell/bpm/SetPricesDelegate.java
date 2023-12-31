package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.kcell.bpm.revision.SetWorkVariables;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log
public class SetPricesDelegate implements TaskListener {
    private static final Map<String, Double> DISCOUNT_MAP = new HashMap<String,Double>() {{
        put("Акмолинская область", 0.20);
        put("Алматинская область", 0.20);
        put("Восточно-Казахстанская область", 0.20);
        put("Жамбылская область", 0.20);
        put("Атырауская область", 0.17);
        put("Мангыстауская область", 0.20);
        put("Западно-Казахстанская область", 0.17);
        put("Туркестанская область", 0.17);
        put("Кызылординская область", 0.04);
        put("Актюбинская область", 0.03);
        put("Карагандинская область", 0.03);
        put("Костанайская область", 0.03);
        put("Северо-Казахстанская область", 0.03);
        put("Павлодарская область", 0.03);
    }};
    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            String mainContract = delegateTask.getVariable("mainContract").toString();
            String priority = delegateTask.getVariable("priority").toString();
            String reason = delegateTask.getVariable("reason").toString();

            ObjectMapper mapper = new ObjectMapper();

            ArrayNode worksPriceList = mapper.createArrayNode();
            Map<String, String> uniqueWorks = new HashMap<>();
            Map<String, String> worksTitleMap = new HashMap<>();

            if("2022Work-agreement".equals(mainContract)){
                if (reason.equals("4")) {
                    delegateTask.addCandidateGroup("hq_operation_approve");
                } else if (Arrays.asList("1", "2", "3", "5", "6").contains(reason)){
                    delegateTask.addCandidateGroup("hq_development_approve");
                }
            }

            if ("Revision".equals(mainContract) || "Roll-out".equals(mainContract)) {

                Map<String, String> worksPriceMap = new HashMap<>();
                InputStream fis = SetPricesDelegate.class.getResourceAsStream("/revision/workPrice.json");

                InputStreamReader reader = new InputStreamReader(fis, "utf-8");
                ArrayNode json = (ArrayNode) mapper.readTree(reader);
                for (JsonNode workPrice : json) {
                    worksPriceMap.put(workPrice.get("id").textValue(), workPrice.get("price").textValue());
                    worksTitleMap.put(workPrice.get("id").textValue(), workPrice.get("title").textValue());
                }

                ArrayNode workPrices = mapper.createArrayNode();
                ArrayNode jobWorks = (ArrayNode) mapper.readTree(delegateTask.getVariable("jobWorks").toString());
                BigDecimal jobWorksTotal = BigDecimal.ZERO;
                for (JsonNode work : jobWorks) {
                    ObjectNode workPrice = work.deepCopy();
                    if (workPrice.get("relatedSites") == null) {
                        workPrice.set("relatedSites", mapper.createArrayNode());
                    }

                    if (!uniqueWorks.containsKey(work.get("sapServiceNumber").textValue())) {
                        String price = worksPriceMap.get(work.get("sapServiceNumber").textValue());
                        String title = worksTitleMap.get(work.get("sapServiceNumber").textValue());

                        ObjectNode workPriceJson = mapper.createObjectNode();
                        workPriceJson.put("sapServiceNumber", work.get("sapServiceNumber").textValue());
                        workPriceJson.put("price", price);
                        workPriceJson.put("title", title);
                        worksPriceList.add(workPriceJson);
                        uniqueWorks.put(work.get("sapServiceNumber").textValue(), "");
                    }

                    BigDecimal unitWorkPrice = new BigDecimal(worksPriceMap.get(work.get("sapServiceNumber").textValue()));

                    if (priority.equals("emergency")) {
                        unitWorkPrice = unitWorkPrice.multiply(new BigDecimal("1.2"));
                    }
                    BigDecimal unitTransportationPrice = unitWorkPrice.multiply(new BigDecimal("0.08"));
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
                    if ("Roll-out".equals(mainContract)) {
                        total = unitWorkPrice.multiply(new BigDecimal(workPrice.get("quantity").asText()));
                    }
                    workPrice.put("unitWorkPrice", unitWorkPrice.setScale(2, RoundingMode.DOWN).toString());
                    workPrice.put("unitWorkPricePlusTx", unitWorkPricePlusTx.setScale(2, RoundingMode.DOWN).toString());
                    workPrice.put("basePriceByQuantity", basePriceByQuantity.setScale(2, RoundingMode.DOWN).toString());
                    workPrice.put("total", total.setScale(2, RoundingMode.DOWN).toString());

                    jobWorksTotal = jobWorksTotal.add(total);

                    workPrice.put("basePrice", worksPriceMap.get(work.get("sapServiceNumber").asText()));
                    workPrices.add(workPrice);
                }
                JsonValue jsonValue = SpinValues.jsonValue(workPrices.toString()).create();

                delegateTask.setVariable("workPrices", jsonValue);
                delegateTask.setVariable("worksPriceList", SpinValues.jsonValue(worksPriceList.toString()).create());
                delegateTask.setVariable("jobWorksTotal", jobWorksTotal.setScale(2, RoundingMode.DOWN).toString());
            } else {
                Object unitWorkPrice_jr_test= delegateTask.getVariable("unitWorkPrice_jr");
                ArrayNode unitWorkPrice_jr=null;
                if(!(unitWorkPrice_jr_test == null)&& !Objects.equals(unitWorkPrice_jr_test.toString(), "null")){
                    unitWorkPrice_jr = (ArrayNode) mapper.readTree(delegateTask.getVariableTyped("unitWorkPrice_jr").getValue().toString());
                }
                Map<String, JsonNode> worksPriceMap = new HashMap<>();

                String siteRegion = (String) delegateTask.getVariable("siteRegion");
                if("nc".equals(siteRegion) || "east".equals(siteRegion)){
                    siteRegion = "astana";
                }
                String oblastName = (String) delegateTask.getVariable("oblastName");
                if(oblastName.contains("Шымкент (г.а.)")) oblastName="Туркестанская область";

                InputStream fis = SetWorkVariables.class.getResourceAsStream("/revision/newWorkPrice.json");
                InputStreamReader reader = new InputStreamReader(fis, "utf-8");
                ArrayNode json = (ArrayNode) mapper.readTree(reader);
                for (JsonNode workPrice : json) {
                    worksPriceMap.put(workPrice.get("id").textValue(), workPrice.get("price"));
                    worksTitleMap.put(workPrice.get("id").textValue(), workPrice.get("title").textValue());
                }

                ArrayNode workPrices = mapper.createArrayNode();
                ArrayNode jobWorks = (ArrayNode) mapper.readTree(delegateTask.getVariable("jobWorks").toString());
                BigDecimal jobWorksTotal = BigDecimal.ZERO;
                int i=0;
                for (JsonNode work : jobWorks) {
                    if(work.has("id") && work.get("id").intValue() >= 9000) {
                        continue;
                    }
                    ObjectNode workPrice = work.deepCopy();
                    if (workPrice.get("relatedSites") == null) {
                        workPrice.set("relatedSites", mapper.createArrayNode());
                    }

                    JsonNode priceJson = worksPriceMap.get(work.get("sapServiceNumber").textValue());
                    if (!uniqueWorks.containsKey(work.get("sapServiceNumber").textValue())) {
                        String title = worksTitleMap.get(work.get("sapServiceNumber").textValue());

                        ObjectNode workPriceJson = mapper.createObjectNode();
                        workPriceJson.put("sapServiceNumber", work.get("sapServiceNumber").textValue());
                        if (Arrays.asList("2022Work-agreement","2023primary_source","Vostoktelecom","open-tender-2023").contains(mainContract)) {
                            workPriceJson.put("priceWithMaterial", priceJson.get(oblastName).textValue());
                            workPriceJson.put("priceWithoutMaterial", priceJson.get(oblastName).textValue());
                            workPriceJson.put("price", priceJson.get(oblastName).textValue());
                        } else if ("technical_maintenance_services".equals(mainContract)) {
                            if(work.get("materials").asBoolean()){
                                workPriceJson.put("price", priceJson.get(oblastName).get(work.get("isMaterialsActive").textValue()).textValue());
                                workPriceJson.put("priceWithMaterial", priceJson.get(oblastName).get(work.get("isMaterialsActive").textValue()).textValue());
                                workPriceJson.put("priceWithoutMaterial", priceJson.get(oblastName).get(work.get("isMaterialsActive").textValue()).textValue());
                            }
                            else {
                                workPriceJson.put("price", priceJson.get(oblastName).get("active").textValue());
                                workPriceJson.put("priceWithMaterial", priceJson.get(oblastName).get("active").textValue());
                                workPriceJson.put("priceWithoutMaterial", priceJson.get(oblastName).get("active").textValue());
                            }
                        } else {
                            workPriceJson.put("priceWithMaterial", priceJson.get(siteRegion).get("with_material").textValue());
                            workPriceJson.put("priceWithoutMaterial", priceJson.get(siteRegion).get("without_material").textValue());
                            workPriceJson.put("price", priceJson.get(siteRegion).get(work.has("materialsProvidedBy") && "subcontractor".equals(work.get("materialsProvidedBy").textValue()) ? "with_material" : "without_material").textValue());
                        }

                        workPriceJson.put("title", title);
                        worksPriceList.add(workPriceJson);
                        uniqueWorks.put(work.get("sapServiceNumber").textValue(), "");
                    }

                    BigDecimal unitWorkPrice = null;
                    String basePrice;
                    if (unitWorkPrice_jr != null && unitWorkPrice_jr.get(i) != null) {
                        String s = String.valueOf(unitWorkPrice_jr.get(i));
                        unitWorkPrice = new BigDecimal(s);
                    }
                    if ("2022Work-agreement".equals(mainContract)) {
                        basePrice = priceJson.get(oblastName).textValue();
                        unitWorkPrice = unitWorkPrice != null ? unitWorkPrice : new BigDecimal(basePrice);
                        if (unitWorkPrice_jr != null && unitWorkPrice_jr.get(i) != null) {
                            double discount= DISCOUNT_MAP.get(oblastName);
                            BigDecimal totalWithDiscount=unitWorkPrice;
                            totalWithDiscount=totalWithDiscount.multiply(new BigDecimal(1.00-discount));
                            totalWithDiscount=totalWithDiscount.multiply(new BigDecimal(workPrice.get("quantity").asText())).setScale(2, RoundingMode.DOWN);
                            workPrice.put("totalWithDiscount",totalWithDiscount.toString());
                        }
                    } else if ("technical_maintenance_services".equals(mainContract)) {
                        if(work.get("materials").asBoolean()){
                            basePrice = priceJson.get(oblastName).get(work.get("isMaterialsActive").textValue()).textValue();
                            unitWorkPrice = unitWorkPrice != null ? unitWorkPrice : new BigDecimal(basePrice);
                        } else if (Arrays.asList("-","").contains(priceJson.get(oblastName).get("active").textValue())) {
                            basePrice = priceJson.get(oblastName).get("inactive").textValue();
                            unitWorkPrice = unitWorkPrice != null ? unitWorkPrice : new BigDecimal(basePrice);
                        } else {
                            basePrice = priceJson.get(oblastName).get("active").textValue();
                            unitWorkPrice = unitWorkPrice != null ? unitWorkPrice : new BigDecimal(basePrice);
                        }
                    } else if (Arrays.asList("2023primary_source","Vostoktelecom","open-tender-2023").contains(mainContract)) {
                        basePrice = priceJson.get(oblastName).textValue();
                        unitWorkPrice = unitWorkPrice != null ? unitWorkPrice : new BigDecimal(basePrice);
                    } else {
                        basePrice = priceJson.get(siteRegion).get("without_material").textValue();
                        unitWorkPrice = unitWorkPrice != null ? unitWorkPrice :
                            new BigDecimal(priceJson.get(siteRegion).get(work.has("materialsProvidedBy") && "subcontractor".equals(work.get("materialsProvidedBy").textValue()) ? "with_material" : "without_material").textValue());
                    }
                    workPrice.put("basePrice", basePrice);
                    workPrices.add(workPrice);

                    if (priority.equals("emergency")) {
                        unitWorkPrice = unitWorkPrice.multiply(new BigDecimal("1.5"));
                    }

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
                    i++;
                }
                JsonValue jsonValue = SpinValues.jsonValue(workPrices.toString()).create();

                delegateTask.setVariable("workPrices", jsonValue);
                delegateTask.setVariable("worksPriceList", SpinValues.jsonValue(worksPriceList.toString()).create());
                delegateTask.setVariable("jobWorksTotal", jobWorksTotal.setScale(2, RoundingMode.DOWN).toString());
            }
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Price calculate error on businessKey: " + delegateTask.getExecution().getBusinessKey(), e);
        }
    }
}
