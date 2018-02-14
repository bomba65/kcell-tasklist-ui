package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceRegionContractorAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        StringBuilder group = new StringBuilder("_contractor_");

        String region = delegateTask.getVariable("region").toString();
        String contractor = delegateTask.getVariable("contractor").toString();
        if (region != null) {
            group.insert(0, region);
            group.append(contractor);
            delegateTask.addCandidateGroup(group.toString());
        }
    }
}
