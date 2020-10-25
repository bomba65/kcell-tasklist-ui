package kz.kcell.flow.changeTsd;

import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

@Log
@Service("setBusinessKeys")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SetBusinessKeys implements JavaDelegate {

    private static final Map<String, String> regionsTitle =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("almaty", "ALM");
            map.put("astana", "AST");
            map.put("north", "NOR");
            map.put("east", "EAS");
            map.put("south", "SOU");
            map.put("west", "WES");
            return Collections.unmodifiableMap(map);
        })).get();

    @Override
    public void execute(DelegateExecution delegateExecution) {

        JSONObject tsd = new JSONObject(delegateExecution.getVariable("selectedTsd").toString());
        String ne_sitename = tsd.getJSONObject("nearend_id").getString("site_name");
        String fe_sitename = tsd.getJSONObject("farend_id").getString("site_name");
        String region_name = tsd.getJSONObject("nearend_id").getJSONObject("facility_id").getJSONObject("address_id").getJSONObject("city_id").getJSONObject("district_id").getJSONObject("oblast_id").getJSONObject("region_id").getString("name").toLowerCase();
        String proc_def_key = delegateExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateExecution.getProcessDefinitionId()).getKey();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_YEAR, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        Date firstDate = c.getTime();

        c.add(Calendar.YEAR, 1);

        Date lastDate = c.getTime();

        Integer count = delegateExecution.getProcessEngineServices()
            .getHistoryService()
            .createHistoricProcessInstanceQuery()
            .processDefinitionKey(proc_def_key)
            .startedAfter(firstDate)
            .startedBefore(lastDate)
            .list()
            .size();

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");

        String businessKey = ne_sitename + "-" + fe_sitename + "(" + regionsTitle.get(region_name) + StringUtils.leftPad(String.valueOf(count+1), 4, '0') + "-" + sdf.format(new Date()) + ")-"+ proc_def_key;
        delegateExecution.setProcessBusinessKey(businessKey);
        delegateExecution.setVariable(proc_def_key + "Number", businessKey);

    }
}
