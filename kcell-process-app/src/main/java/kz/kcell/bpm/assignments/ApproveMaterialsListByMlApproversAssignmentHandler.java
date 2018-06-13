package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class ApproveMaterialsListByMlApproversAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String siteRegion = String.valueOf(delegateTask.getVariable("siteRegion"));
        String reason = String.valueOf(delegateTask.getVariable("reason"));

        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup(siteRegion + "_optimization_mlapprover");
            } else if (reason.equals("2")) {
                delegateTask.addCandidateGroup(siteRegion + "_transmission_mlapprover");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup(siteRegion + "_infrastructure_mlapprover");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup(siteRegion + "_sao_mlapprover");
            }
        }
    }
}
