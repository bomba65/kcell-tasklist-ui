package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceRegionContractorAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String contractor = delegateTask.getVariable("contractor").toString();
        String region = delegateTask.getVariable("region").toString();

        if("Kcell_region".equals(contractor)){
            delegateTask.addCandidateGroup(region + "_engineer");
        } else {
            StringBuilder group = new StringBuilder("_contractor_");

            if (region != null) {
                group.insert(0, ("nc".equals(region)?"astana":region));
                group.append(contractor);
                delegateTask.addCandidateGroup(group.toString());
            }
        }
    }
}
