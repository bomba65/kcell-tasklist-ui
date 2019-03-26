package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class DeliveryBulkSMSPartnerOrB2BAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        Boolean partner = Boolean.parseBoolean(delegateTask.getVariable("isPartner").toString());
        if (partner == true) {
            delegateTask.addCandidateGroup("delivery_sms_partner");
        } else{
            delegateTask.addCandidateGroup("delivery_sms_b2b_delivery");
        }
    }
}
