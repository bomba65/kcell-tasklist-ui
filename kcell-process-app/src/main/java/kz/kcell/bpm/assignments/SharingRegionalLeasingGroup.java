package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class SharingRegionalLeasingGroup implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        if (siteRegion != null) {
            delegateTask.addCandidateGroup(siteRegion + "_Kcell_regional_leasing_group");
        }
    }
}
