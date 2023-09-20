package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class ApproveMaterialsListByMlApproversAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String siteRegion = String.valueOf(delegateTask.getVariable("siteRegion"));
        String reason = String.valueOf(delegateTask.getVariable("reason"));

        if (reason != null) {
            if (reason.equals("2")) {
                delegateTask.addCandidateGroup(siteRegion + "_transmission_tr");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup(siteRegion + "_operation_tr");
            } else if (Arrays.asList("1", "3", "5").contains(reason)){
                delegateTask.addCandidateGroup(siteRegion + "_development_tr");
            }
        }
    }
}
