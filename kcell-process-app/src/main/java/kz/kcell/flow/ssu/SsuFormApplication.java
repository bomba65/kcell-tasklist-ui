package kz.kcell.flow.ssu;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class SsuFormApplication implements JavaDelegate {

    @Autowired
    HistoryService historyService;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String ssu_id = delegateExecution.getVariable("ssu_id").toString();
        String bin = delegateExecution.getVariable("bin").toString();

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.add(Calendar.HOUR, 6);
        Date current = dateCalendar.getTime();

        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy");

        long count = historyService
            .createHistoricProcessInstanceQuery()
            .processDefinitionKey("SSU")
            .processInstanceBusinessKeyLike(ssu_id + "_" + bin + "_" + format.format(current) + "_%")
            .count();

        delegateExecution.setProcessBusinessKey(ssu_id + "_" + bin + "_" + format.format(current) + "_" + (count+1));
    }
}
