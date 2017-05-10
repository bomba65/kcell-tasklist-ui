package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SetPricesDelegate implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        try {
            Map<String, Double> worksMap = new HashMap<>();

            ObjectMapper mapper = new ObjectMapper();

            InputStream fis = SetPricesDelegate.class.getResourceAsStream("/workPrice.json");
            InputStreamReader reader = new InputStreamReader(fis, "utf-8");
            ArrayNode json = (ArrayNode) mapper.readTree(reader);
            for (JsonNode workPrice : json) {
                worksMap.put(workPrice.get("id").textValue(), Double.valueOf(workPrice.get("price").textValue()));
            }

            ArrayNode workPrices = mapper.createArrayNode();
            System.out.println(delegateTask.getVariable("jobWorks").toString());
            ArrayNode jobWorks = (ArrayNode) mapper.readTree(delegateTask.getVariable("jobWorks").toString());

            for (JsonNode work : jobWorks) {
                JsonNode workPrice = mapper.readTree("{\"sapServiceNumber\": \"" + work.get("sapServiceNumber").textValue() + "\", \"price\":" + Double.valueOf(worksMap.get(work.get("sapServiceNumber").textValue())).longValue() + "}");
                workPrices.add(workPrice);
            }

            JsonValue jsonValue = SpinValues.jsonValue(workPrices.toString()).create();

            delegateTask.setVariable("workPrices", jsonValue);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
