package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceCenterGroupHeadAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("workType").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup("hq_optimization_head");
            } else if (reason.equals("2")) {
                //идет по другой ветке
            } else if (reason.equals("3")) {
                //идет по другой ветке
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("hq_operation_head");
            }
        }
    }
}
