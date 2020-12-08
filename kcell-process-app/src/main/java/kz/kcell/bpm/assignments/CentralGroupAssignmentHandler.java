package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class CentralGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup("hq_optimization");
            } else if (reason.equals("2")) {
                //Данные работы идут по другой ветке
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup("hq_infrastructure");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("hq_operation");
            } else if (reason.equals("5")) {
                delegateTask.addCandidateGroup("hq_rollout");
            }
        }
    }
}
