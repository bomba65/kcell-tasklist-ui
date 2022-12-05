package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class ApproveMaterialsListByTnuRegionAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String siteRegion = String.valueOf(delegateTask.getVariable("siteRegion"));
        if (siteRegion != null) {
            delegateTask.addCandidateGroup(siteRegion + "_transmission_mlapprover");
        }
    }
}
