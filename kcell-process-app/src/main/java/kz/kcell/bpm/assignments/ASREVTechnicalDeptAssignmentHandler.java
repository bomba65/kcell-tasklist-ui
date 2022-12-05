package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;

public class ASREVTechnicalDeptAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        SpinJsonNode tsSpinNode = delegateTask.<JsonValue>getVariableTyped("techSpecs").getValue();
        String connectionPoint  = tsSpinNode.prop("connectionPoint").stringValue();
        if (connectionPoint != null) {
            if (connectionPoint.equals("SBC")) {
                delegateTask.addCandidateGroup("delivery_pbx_sbc_technical_dept");
            } else if (connectionPoint.equals("SIP Proxy")) {
                delegateTask.addCandidateGroup("delivery_pbx_sip_proxy_technical_dept");
            }
        }
    }
}


