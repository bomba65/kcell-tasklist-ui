package kz.kcell.bpm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.spin.plugin.variable.SpinValues;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class StartRevisionProcess implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        execution.setVariable("isNewProcessCreated", "true");
        execution.setVariable("prCreationInProgress", "false");

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode worksPriceList = mapper.createArrayNode();
        Map<String, String> uniqueWorks = new HashMap<>();
        Map<String, String> worksPriceMap = new HashMap<>();
        Map<String, String> worksTitleMap = new HashMap<>();

        InputStream fis = SetPricesDelegate.class.getResourceAsStream("/workPrice.json");

        InputStreamReader reader = new InputStreamReader(fis, "utf-8");
        ArrayNode json = (ArrayNode) mapper.readTree(reader);
        for (JsonNode workPrice : json) {
            worksPriceMap.put(workPrice.get("id").textValue(), workPrice.get("price").textValue());
            worksTitleMap.put(workPrice.get("id").textValue(), workPrice.get("title").textValue());
        }

        ArrayNode jobWorks = (ArrayNode) mapper.readTree(execution.getVariable("jobWorks").toString());
        for (JsonNode work : jobWorks) {
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
        }
        execution.setVariable("worksPriceList", SpinValues.jsonValue(worksPriceList.toString()).create());
    }
}
