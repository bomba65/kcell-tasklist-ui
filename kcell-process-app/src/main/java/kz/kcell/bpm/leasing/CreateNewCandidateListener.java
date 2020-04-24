package kz.kcell.bpm.leasing;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class CreateNewCandidateListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        delegateTask.addCandidateGroup(siteRegion + "_planning");
    }
}
