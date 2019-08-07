package kz.kcell.bpm.dismantleReplace;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class SetCentralGroupsTaskResult implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String centralGroupsResult = "";
        if(delegateTask.hasVariable("centralGroupsResult")){
            centralGroupsResult = delegateTask.getVariable("centralGroupsResult").toString();
        }

        String central_groupsTaskResult = delegateTask.getVariable("central_groupsTaskResult").toString();

        if("reject".equals(central_groupsTaskResult)){
            delegateTask.setVariable("centralGroupsResult", central_groupsTaskResult);
        } else if("returnForCorrection".equals(central_groupsTaskResult) && !"reject".equals(centralGroupsResult)) {
            delegateTask.setVariable("centralGroupsResult", central_groupsTaskResult);
        } else if("approve".equals(central_groupsTaskResult) && ("".equals(centralGroupsResult) || "approve".equals(centralGroupsResult))) {
            delegateTask.setVariable("centralGroupsResult", central_groupsTaskResult);
        }
    }
}
