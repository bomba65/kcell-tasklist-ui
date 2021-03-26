package kz.kcell.flow.ssu;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class SsuFormApplication implements JavaDelegate {

    @Autowired
    HistoryService historyService;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String ssu_id = delegateExecution.getVariable("ssu_id").toString();
        String bin = delegateExecution.getVariable("bin").toString();

        if(delegateExecution.hasVariable("contract_date") && delegateExecution.getVariable("contract_date")!=null){
            Date contract_date = (Date) delegateExecution.getVariable("contract_date");

            SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");

            long count = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey("SSU")
                .processInstanceBusinessKeyLike(ssu_id + "_" + bin + "_" + format.format(contract_date) + "_%")
                .count();

            delegateExecution.setProcessBusinessKey(ssu_id + "_" + bin + "_" + format.format(contract_date) + "_" + (count+1));
        }
    }
}
