package kz.kcell.bpm.assignments.dismantleReplace;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class RegionHeadAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String requestType = (String) delegateTask.getVariable("requestType");

        if("dismantle".equals(requestType)){
            delegateTask.setDescription("SITE DISMANTLING REQUEST");
        } else if("replace".equals(requestType)){
            delegateTask.setDescription("SITE REPLACE REQUEST");
        }
    }
}
