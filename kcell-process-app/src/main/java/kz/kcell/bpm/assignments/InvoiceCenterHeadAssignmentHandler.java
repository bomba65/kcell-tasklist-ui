package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceCenterHeadAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("workType").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                //идет по другой ветке
            } else if (reason.equals("2")) {
                delegateTask.addCandidateGroup("hq_transmission_head");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup("hq_infrastructure_head");
            } else if (reason.equals("4")) {
                //идет по другой ветке
            }
        }
    }
}
