package kz.kcell.flow.tnuTsd;

import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

@Log
@Service("tnuTsdSetBusinessKey")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SetBusinessKey implements JavaDelegate {

    private static final Map<String, String> regionsTitle =
        ((Supplier<Map<String, String>>) (() -> {
            Map<String, String> map = new HashMap<>();
            map.put("alm", "ALM");
            map.put("astana", "AST");
            map.put("nc", "NOR");
            map.put("east", "EAS");
            map.put("south", "SOU");
            map.put("west", "WES");
            return Collections.unmodifiableMap(map);
        })).get();

    @Override
    public void execute(DelegateExecution delegateExecution) {

        String ne_sitename = delegateExecution.getVariable("ne_sitename").toString();
        String fe_sitename = delegateExecution.getVariable("fe_sitename").toString();
        String region_name = delegateExecution.getVariable("region_name").toString();

        Long count1 = delegateExecution.getProcessEngineServices()
            .getHistoryService()
            .createHistoricProcessInstanceQuery()
            .processDefinitionKey("tnu_tsd_db")
            .variableValueEquals("region_name", region_name)
            .count();

        Long count = delegateExecution.getProcessEngineServices()
            .getHistoryService()
            .createHistoricProcessInstanceQuery()
            .processDefinitionKey("create-new-tsd")
            .variableValueEquals("region_name", region_name)
            .count();

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 6);
        String businessKey = ne_sitename + "-" + fe_sitename + "(" + regionsTitle.get(region_name) + StringUtils.leftPad(String.valueOf(count1 + count + 1), 4, '0') + "-" + sdf.format(calendar.getTime()) + ")";
        delegateExecution.setProcessBusinessKey(businessKey);
        delegateExecution.setVariable("tnuTsdNumber", businessKey);

    }
}
