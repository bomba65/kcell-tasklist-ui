package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class CentralGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();

        if (reason != null) {
            if (reason.equals("2")) {
                delegateTask.addCandidateGroup("hq_transmission_approve");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("hq_operation_approve");
            } else if (Arrays.asList("1", "3", "5","6").contains(reason)){
                delegateTask.addCandidateGroup("hq_development_approve");
            }
        }
    }
}
