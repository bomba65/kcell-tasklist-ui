package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class DeliveryAfterSalesOldProviderAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String oldProvider = delegateTask.getVariable("oldProvider").toString();
        if (oldProvider != null) {
            if (oldProvider.equals("MMS")) {
                delegateTask.addCandidateGroup("delivery_provider_mms");
            } else if (oldProvider.equals("KIT")) {
                delegateTask.addCandidateGroup("delivery_provider_kit");
            } else if (oldProvider.equals("SMS Consult")) {
                delegateTask.addCandidateGroup("delivery_provider_sms_consult");
            }
        }
    }
}
