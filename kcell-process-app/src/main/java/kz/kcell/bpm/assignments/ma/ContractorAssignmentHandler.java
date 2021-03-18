package kz.kcell.bpm.assignments.ma;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class ContractorAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String contractor = delegateTask.getVariable("contractor").toString();
        if (contractor != null) {
            delegateTask.addCandidateGroup("hq_contractor_" + contractor);
        }
    }
}
