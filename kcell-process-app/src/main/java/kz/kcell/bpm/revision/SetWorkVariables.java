package kz.kcell.bpm.revision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.kcell.bpm.SetPricesDelegate;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.spin.plugin.variable.SpinValues;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Log
public class SetWorkVariables implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode worksPriceList = mapper.createArrayNode();
        Map<String, String> uniqueWorks = new HashMap<>();
        StringBuilder workTitlesForSearch = new StringBuilder("");

        String mainContract = execution.getVariable("mainContract").toString();

        if("Revision".equals(mainContract) || "Roll-out".equals(mainContract)){
            Map<String, String> worksPriceMap = new HashMap<>();
            Map<String, String> worksTitleMap = new HashMap<>();

            InputStream fis = SetWorkVariables.class.getResourceAsStream("/revision/workPrice.json");

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

                    if(workTitlesForSearch.length() > 0){
                        workTitlesForSearch.append(", ");
                    }
                    workTitlesForSearch.append(title);
                }
            }
            execution.setVariable("worksPriceList", SpinValues.jsonValue(worksPriceList.toString()).create());
            execution.setVariable("workTitlesForSearch", workTitlesForSearch.toString());
        } else {
            Map<String, JsonNode> worksPriceMap = new HashMap<>();
            Map<String, String> worksTitleMap = new HashMap<>();

            InputStream fis = SetWorkVariables.class.getResourceAsStream("/revision/newWorkPrice.json");
            InputStreamReader reader = new InputStreamReader(fis, "utf-8");
            ArrayNode json = (ArrayNode) mapper.readTree(reader);

            for (JsonNode workPrice : json) {
                worksPriceMap.put(workPrice.get("id").textValue(), workPrice.get("price"));
                worksTitleMap.put(workPrice.get("id").textValue(), workPrice.get("title").textValue());
            }

            ArrayNode jobWorks = (ArrayNode) mapper.readTree(execution.getVariable("jobWorks").toString());
            for (JsonNode work : jobWorks) {
                if(!uniqueWorks.containsKey(work.get("sapServiceNumber").textValue())){
                    JsonNode priceJson = worksPriceMap.get(work.get("sapServiceNumber").textValue());
                    String title = worksTitleMap.get(work.get("sapServiceNumber").textValue());

                    ObjectNode workPriceJson = mapper.createObjectNode();
                    workPriceJson.put("sapServiceNumber", work.get("sapServiceNumber").textValue());

                    if ("2022Work-agreement".equals(mainContract)) {
                        String oblast = execution.hasVariable("oblastName") ? execution.getVariable("oblastName").toString() : null;
                        workPriceJson.put("price", priceJson.get(oblast).textValue());
                    } else {
                        String siteRegion = (String) execution.getVariable("siteRegion");
                        if("nc".equals(siteRegion) || "east".equals(siteRegion)){
                            siteRegion = "astana";
                        }
                        workPriceJson.put("price", priceJson.get(siteRegion).get(work.has("materialsProvidedBy") && "subcontractor".equals(work.get("materialsProvidedBy").textValue())?"with_material":"without_material").textValue());
                    }
                    workPriceJson.put("title", title);
                    worksPriceList.add(workPriceJson);
                    uniqueWorks.put(work.get("sapServiceNumber").textValue(), "");

                    if(workTitlesForSearch.length() > 0){
                        workTitlesForSearch.append(", ");
                    }
                    workTitlesForSearch.append(title);
                }
            }
            execution.setVariable("worksPriceList", SpinValues.jsonValue(worksPriceList.toString()).create());
            execution.setVariable("workTitlesForSearch", workTitlesForSearch.toString());
        }
    }
}
