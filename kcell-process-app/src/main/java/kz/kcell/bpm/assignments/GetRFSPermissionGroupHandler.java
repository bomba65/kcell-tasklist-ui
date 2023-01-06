package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class GetRFSPermissionGroupHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.addCandidateGroup("hq_permission");
    }

}
