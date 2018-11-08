package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class DeliveryFreephoneBulkSMSProviderAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String provider = delegateTask.getVariable("provider").toString();
        if (provider != null) {
            if (provider.equals("MMS")) {
                delegateTask.addCandidateGroup("delivery_provider_mms");
            } else if (provider.equals("KIT")) {
                delegateTask.addCandidateGroup("delivery_provider_kit");
            } else if (provider.equals("SMS Consult")) {
                delegateTask.addCandidateGroup("delivery_provider_sms_consult");
            }
        }
    }
}
