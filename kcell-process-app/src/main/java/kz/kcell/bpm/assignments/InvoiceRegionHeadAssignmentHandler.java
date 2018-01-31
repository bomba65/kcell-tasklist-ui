package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceRegionHeadAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("workType").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup("alm_optimization_head");
            } else if (reason.equals("2")) {
                delegateTask.addCandidateGroup("alm_transmission_head");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup("alm_infrastructure_head");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("alm_operation_head");
            }
        }
    }
}
