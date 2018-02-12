package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceRegionContractorAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        StringBuilder group = new StringBuilder("_contractor_lse");

        String region = delegateTask.getVariable("region").toString();
        if (region != null) {
            group.insert(0, region);
            delegateTask.addCandidateGroup(group.toString());
        }
    }
}
