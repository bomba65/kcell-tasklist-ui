package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

@lombok.extern.java.Log
public class ApproveMaterialsListByCentralGroupsHeadAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {


        String group = String.valueOf(delegateTask.getVariable("group"));

        if (group != null) {
            if (group.equals("\"P&O\"")) {
                delegateTask.addCandidateGroup("hq_optimization");
            } else if (group.equals("\"Transmission\"")) {
                delegateTask.addCandidateGroup("hq_transmission_engineer");
            } else if (group.equals("\"S&FM\"")) {
                delegateTask.addCandidateGroup("hq_infrastructure");
            } else if (group.equals("\"Operation\"")) {
                delegateTask.addCandidateGroup("hq_operation");
            }
        }
    }
}
