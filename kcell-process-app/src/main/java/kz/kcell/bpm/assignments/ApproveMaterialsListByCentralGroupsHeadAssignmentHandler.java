package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

@lombok.extern.java.Log
public class ApproveMaterialsListByCentralGroupsHeadAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {


        String group = String.valueOf(delegateTask.getVariable("group"));
        String reason = delegateTask.getVariable("reason").toString();

        if (reason != null) {
            if (Arrays.asList("1", "3","5").contains(reason)) {
                if (group.equals("\"Operation\"")) {
                    delegateTask.addCandidateGroup("hq_operation_tr");
                } else if (group.equals("\"Transmission\"")) {
                    delegateTask.addCandidateGroup("hq_transmission_tr");
                }
            } else if (reason.equals("2")) {
                if (group.equals("\"Operation\"")) {
                    delegateTask.addCandidateGroup("hq_operation_tr");
                } else if (group.equals("\"Development\"")) {
                    delegateTask.addCandidateGroup("hq_development_tr");
                }
            } else if (reason.equals("4")) {
                if (group.equals("\"Transmission\"")) {
                    delegateTask.addCandidateGroup("hq_transmission_tr");
                } else if (group.equals("\"Development\"")) {
                    delegateTask.addCandidateGroup("hq_development_tr");
                }
            }
        }
    }
}
