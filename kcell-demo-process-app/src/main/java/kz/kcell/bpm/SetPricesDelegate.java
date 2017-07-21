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
            Map<String, String> worksMap = new HashMap<>();

            ObjectMapper mapper = new ObjectMapper();

            InputStream fis = SetPricesDelegate.class.getResourceAsStream("/workPrice.json");
            InputStreamReader reader = new InputStreamReader(fis, "utf-8");
            ArrayNode json = (ArrayNode) mapper.readTree(reader);
            for (JsonNode workPrice : json) {
                worksMap.put(workPrice.get("id").textValue(), workPrice.get("price").textValue());
            }

            ArrayNode workPrices = mapper.createArrayNode();
            System.out.println(delegateTask.getVariable("jobWorks").toString());
            ArrayNode jobWorks = (ArrayNode) mapper.readTree(delegateTask.getVariable("jobWorks").toString());
            BigDecimal jobWorksTotal = BigDecimal.ZERO;
            for (JsonNode work : jobWorks) {
                ObjectNode workPrice = work.deepCopy();
                if (workPrice.get("relatedSites") == null) {
                    workPrice.set("relatedSites", mapper.createArrayNode());
                }
                BigDecimal unitWorkPrice = new BigDecimal(worksMap.get(work.get("sapServiceNumber").textValue()));
                BigDecimal unitWorkPricePlusTx = unitWorkPrice.multiply(new BigDecimal("1.08"));
                if (workPrice.get("relatedSites").size() > 0) {
                    BigDecimal unitWorkPricePerSite = unitWorkPricePlusTx.divide(new BigDecimal(workPrice.get("relatedSites").size()));
                    System.out.println(unitWorkPricePerSite);
                    System.out.println(workPrice.get("quantity").asText());
                    System.out.println(new BigDecimal(workPrice.get("quantity").asText()));
                    BigDecimal netWorkPricePerSite = unitWorkPricePerSite.multiply(new BigDecimal(workPrice.get("quantity").asText()));
                    workPrice.put("unitWorkPricePerSite", unitWorkPricePerSite.setScale(2, RoundingMode.CEILING).toString());
                    workPrice.put("netWorkPricePerSite", netWorkPricePerSite.setScale(2, RoundingMode.CEILING).toString());
                }

                BigDecimal total = unitWorkPricePlusTx.multiply(new BigDecimal(workPrice.get("quantity").asText()));
                workPrice.put("unitWorkPrice", unitWorkPrice.setScale(2, RoundingMode.CEILING).toString());
                workPrice.put("unitWorkPricePlusTx", unitWorkPricePlusTx.setScale(2, RoundingMode.CEILING).toString());
                workPrice.put("total", total.setScale(2, RoundingMode.CEILING).toString());

                jobWorksTotal = jobWorksTotal.add(total);

                workPrice.put("basePrice", worksMap.get(work.get("sapServiceNumber").asText()));
                workPrices.add(workPrice);
            }
            JsonValue jsonValue = SpinValues.jsonValue(workPrices.toString()).create();

            delegateTask.setVariable("workPrices", jsonValue);
            System.out.println(jobWorksTotal);
            delegateTask.setVariable("jobWorksTotal", jobWorksTotal.setScale(2, RoundingMode.CEILING).toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
