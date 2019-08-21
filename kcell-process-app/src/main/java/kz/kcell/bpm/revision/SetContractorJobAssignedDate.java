package kz.kcell.bpm.revision;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Date;

public class SetContractorJobAssignedDate implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        if(delegateTask.hasVariable("contractorJobAssignedDate")){
            Date contractorJobAssignedDate = (Date) delegateTask.getVariable("contractorJobAssignedDate");

            long afterModifyList = delegateTask.getProcessEngineServices()
                .getHistoryService()
                .createHistoricTaskInstanceQuery()
                .processInstanceId(delegateTask.getProcessInstanceId())
                .taskDefinitionKey("modify_jr")
                .finishedAfter(contractorJobAssignedDate)
                .count();

            if(afterModifyList > 0){
                delegateTask.setVariable("contractorJobAssignedDate", new Date());
            }
        } else {
            delegateTask.setVariable("contractorJobAssignedDate", new Date());
        }

        String mainContract = String.valueOf(delegateTask.getVariable("mainContract"));
        if("Roll-out".equals(mainContract)){
            delegateTask.setVariable("rolloutRO1", "false");
            delegateTask.setVariable("rolloutRO2", "false");
            delegateTask.setVariable("rolloutRO3", "false");
            delegateTask.setVariable("rolloutRO4", "false");
        }
    }
}
