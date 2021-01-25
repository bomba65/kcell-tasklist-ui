package kz.kcell.bpm.revision;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetContractorJobAssignedDate implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 6);

        if (delegateTask.hasVariable("contractorJobAssignedDate")) {
            Date contractorJobAssignedDate = (Date) delegateTask.getVariable("contractorJobAssignedDate");

            long afterModifyList = delegateTask.getProcessEngineServices()
                .getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId(delegateTask.getProcessInstanceId())
                .taskDefinitionKey("modify_jr")
                .finishedAfter(contractorJobAssignedDate)
                .count();


            if (afterModifyList > 0) {
                delegateTask.setVariable("contractorJobAssignedDate", calendar.getTime());
            }
        } else {
            delegateTask.setVariable("contractorJobAssignedDate", calendar.getTime());
        }

        String mainContract = String.valueOf(delegateTask.getVariable("mainContract"));
        if ("Roll-out".equals(mainContract)) {
            if (!delegateTask.hasVariable("rolloutRO1")) {
                delegateTask.setVariable("rolloutRO1", "false");
            }
            if (!delegateTask.hasVariable("rolloutRO2")) {
                delegateTask.setVariable("rolloutRO2", "false");
            }
            if (!delegateTask.hasVariable("rolloutRO3")) {
                delegateTask.setVariable("rolloutRO3", "false");
            }
            if (!delegateTask.hasVariable("rolloutRO4")) {
                delegateTask.setVariable("rolloutRO4", "false");
            }
            String workTitlesForSearch = delegateTask.getVariable("workTitlesForSearch").toString();
            if (workTitlesForSearch.contains("13.")) {
                delegateTask.setVariable("contains13Work", true);
            } else {
                delegateTask.setVariable("contains13Work", false);
            }
        }
    }
}
