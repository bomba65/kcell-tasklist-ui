package kz.kcell.bpm.invoice;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;

public class SetRolloutActStatus implements JavaDelegate {

    Expression status;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String status = this.status.getValue(delegateExecution).toString();
        String mainContract = String.valueOf(delegateExecution.getVariable("mainContract"));

        if ("Roll-out".equals(mainContract)) {
            RuntimeService runtimeService = delegateExecution.getProcessEngineServices().getRuntimeService();
            SpinJsonNode selectedRevisions = delegateExecution.<JsonValue>getVariableTyped("selectedRevisions").getValue();
            String rolloutActType = String.valueOf(delegateExecution.getVariable("rolloutActType"));

            if("RO-1".equals(rolloutActType)){
                for(String revisionId: selectedRevisions.fieldNames()){
                    if("true".equals(status)){
                        runtimeService.setVariable(revisionId, "invoiceRO1Number", delegateExecution.getVariable("invoiceNumber"));
                    } else {
                        runtimeService.setVariable(revisionId, "invoiceRO1Number", "");
                    }
                }
            } else if("RO-2".equals(rolloutActType)){
                for(String revisionId: selectedRevisions.fieldNames()){
                    if("true".equals(status)){
                        runtimeService.setVariable(revisionId, "invoiceRO2Number", delegateExecution.getVariable("invoiceNumber"));
                    } else {
                        runtimeService.setVariable(revisionId, "invoiceRO2Number", "");
                    }
                }
            } else if("RO-3".equals(rolloutActType)){
                for(String revisionId: selectedRevisions.fieldNames()){
                    if("true".equals(status)){
                        runtimeService.setVariable(revisionId, "invoiceRO3Number", delegateExecution.getVariable("invoiceNumber"));
                    } else {
                        runtimeService.setVariable(revisionId, "invoiceRO3Number", "");
                    }
                }
            } else if("RO-4".equals(rolloutActType)){
                for(String revisionId: selectedRevisions.fieldNames()){
                    if("true".equals(status)){
                        runtimeService.setVariable(revisionId, "invoiceRO4Number", delegateExecution.getVariable("invoiceNumber"));
                    } else {
                        runtimeService.setVariable(revisionId, "invoiceRO4Number", "");
                    }
                }
            }
        }

        if("false".equals(status)){
            delegateExecution.setVariable("status", "cancelled");
        } else {
            delegateExecution.setVariable("status", "correct");
        }
    }
}
