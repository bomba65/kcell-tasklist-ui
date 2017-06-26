package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class UpdatePRStatusBySapSpecialistAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup("hq_sap_specialist_optimization");
            } else if (reason.equals("2")) {
                delegateTask.addCandidateGroup("hq_sap_specialist_transmission");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup("hq_sap_specialist_infrastructure");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("hq_sap_specialist_operation");
            }
        }
    }
}
