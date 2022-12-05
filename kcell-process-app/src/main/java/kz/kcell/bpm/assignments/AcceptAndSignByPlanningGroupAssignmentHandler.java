package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class AcceptAndSignByPlanningGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup(siteRegion + "_optimization_planning");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup(siteRegion + "_infrastructure_planning");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup(siteRegion + "_sao_planning");
            }
        }
    }
}