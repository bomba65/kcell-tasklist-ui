package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceManagerAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("workType").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup("hq_manager_ni");
            } else if (reason.equals("2")) {
                delegateTask.addCandidateGroup("hq_manager_ni");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup("hq_manager_ni");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("hq_manager_sao");
            }
        }
    }
}
