package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceRegionManagerAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String region = delegateTask.getVariable("region").toString();
        if (region != null) {
            delegateTask.addCandidateGroup(region + "_manager");
        }
    }
}
