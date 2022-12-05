package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class DeliveryFreephonePartnerOrB2BAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        Boolean partner = Boolean.parseBoolean(delegateTask.getVariable("isPartner").toString());
        if (partner == true) {
            delegateTask.addCandidateGroup("delivery_ivr_partner");
        } else{
            delegateTask.addCandidateGroup("delivery_ivr_b2b_delivery");
        }
    }
}
