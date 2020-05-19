package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SetPricesDelegate implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            String mainContract = delegateTask.getVariable("mainContract").toString();

            ObjectMapper mapper = new ObjectMapper();

            ArrayNode worksPriceList = mapper.createArrayNode();
            Map<String, String> uniqueWorks = new HashMap<>();
            Map<String, String> worksPriceMap = new HashMap<>();
            Map<String, String> worksTitleMap = new HashMap<>();

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

                if(!uniqueWorks.containsKey(work.get("sapServiceNumber").textValue())){
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

                BigDecimal unitTransportationPrice = BigDecimal.valueOf(0);

                if (!"Roll-out".equals(mainContract)){
                    unitTransportationPrice = unitWorkPrice.multiply(new BigDecimal("0.08"));
                    unitTransportationPrice = unitTransportationPrice.setScale(2, RoundingMode.DOWN);
                }

                BigDecimal unitWorkPricePlusTx = unitWorkPrice.add(unitTransportationPrice);

                if (workPrice.get("relatedSites").size() > 0) {
                    BigDecimal unitWorkPricePerSite = unitWorkPricePlusTx.divide(new BigDecimal(workPrice.get("relatedSites").size()), 2, RoundingMode.DOWN);
                    BigDecimal netWorkPricePerSite = unitWorkPricePerSite.multiply(new BigDecimal(workPrice.get("quantity").asText()));
                    workPrice.put("unitWorkPricePerSite", unitWorkPricePerSite.setScale(2, RoundingMode.DOWN).toString());
                    workPrice.put("netWorkPricePerSite", netWorkPricePerSite.setScale(2, RoundingMode.DOWN).toString());
                }

                BigDecimal basePriceByQuantity = unitWorkPrice.multiply(new BigDecimal(workPrice.get("quantity").asText()));

                BigDecimal total = unitWorkPricePlusTx.multiply(new BigDecimal(workPrice.get("quantity").asText()));
                if("Roll-out".equals(mainContract)){
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

        } catch (Exception e) {
            e.printStackTrace();;
            throw new RuntimeException("Price calculate error on businessKey: " + delegateTask.getExecution().getBusinessKey());
        }
    }
}
