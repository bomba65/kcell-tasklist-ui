package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class RevolvingNumbersTrunkAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String connectionPoint  = (String) delegateTask.getVariable("connectionPoint");

        if (connectionPoint != null) {
            if (connectionPoint.equals("SBC")) {
                delegateTask.addCandidateGroup("delivery_pbx_sbc_technical_dept");
            } else if (connectionPoint.equals("SIP Proxy")) {
                delegateTask.addCandidateGroup("delivery_pbx_sip_proxy_technical_dept");
            } else if (connectionPoint.equals("Asterisk")) {
                delegateTask.addCandidateGroup("delivery_pbx_asterisk_technical_dept");
            }
        }
    }
}
