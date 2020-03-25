package kz.kcell.bpm.assignments.tnuTsd;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class RegionEngineerAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String region_name = delegateTask.getVariable("region_name").toString();
        delegateTask.addCandidateGroup(region_name + "_tn_engineer");
    }
}
