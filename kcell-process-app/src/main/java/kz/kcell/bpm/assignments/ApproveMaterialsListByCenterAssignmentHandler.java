package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class ApproveMaterialsListByCenterAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String reason = String.valueOf(delegateTask.getVariable("reason"));

        if (reason != null) {
            if (reason.equals("2")) {
                delegateTask.addCandidateGroup("transmission_mlapprove");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("operation_mlapprove");
            } else if (Arrays.asList("1", "3", "5").contains(reason)){
                delegateTask.addCandidateGroup("development_mlapprove");
            }
        }
    }
}
