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
            .processDefinitionKey("create-new-tsd")
            .startedAfter(firstDate)
            .startedBefore(lastDate)
            .variableValueEquals("region_name", region_name)
            .list()
            .size();

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy");

        String businessKey = ne_sitename + "-" + fe_sitename + "(" + regionsTitle.get(region_name) + StringUtils.leftPad(String.valueOf(count+1), 4, '0') + "-" + sdf.format(new Date()) + ")";
        delegateExecution.setProcessBusinessKey(businessKey);
        delegateExecution.setVariable("tnuTsdNumber", businessKey);

    }
}
