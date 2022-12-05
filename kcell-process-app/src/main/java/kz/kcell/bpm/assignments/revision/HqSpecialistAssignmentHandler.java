package kz.kcell.bpm.assignments.revision;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class HqSpecialistAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup("hq_optimization_specialist");
            } else if (reason.equals("2")) {
                delegateTask.addCandidateGroup("hq_transmission_budget");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup("hq_infrastructure_specialist");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("hq_operation_specialist");
            }
        }
    }
}
