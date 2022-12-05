package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class RevisionDefineInitiatorGroupHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String reason = String.valueOf(delegateTask.getVariable("reason"));

        if (reason != null) {
            if(reason.equals("5")) {
                delegateTask.addCandidateGroup(siteRegion + "_rollout");        
            } else {
                delegateTask.addCandidateGroup(siteRegion + "_engineer");
            }
        } else {
            delegateTask.addCandidateGroup(siteRegion + "_engineer");
        }
        
    }
}
