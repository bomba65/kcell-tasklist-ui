package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class RegionHqLeasingAssignmentHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String siteObjectType = delegateTask.getVariable("siteObjectType").toString();
        if (siteObjectType.equals("national")) {
            delegateTask.addCandidateGroup("hq_leasing");
        } else {
            delegateTask.addCandidateGroup(siteRegion + "_leasing");
        }
    }
}
