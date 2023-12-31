package kz.kcell.bpm.leasing;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class CheckFEListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String ncpID = delegateTask.getVariable("ncpID").toString();
        if (ncpID != null && ncpID.startsWith("11") && !siteRegion.equals("astana")) {
            siteRegion = "astana";
            delegateTask.setVariable("siteRegion", siteRegion);
        }
        delegateTask.addCandidateGroup(siteRegion + "_transmission");
    }
}
