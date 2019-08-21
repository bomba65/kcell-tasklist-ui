package kz.kcell.bpm;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SetInvoicingStatus implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        RuntimeService runtimeService = delegateExecution.getProcessEngineServices().getRuntimeService();

        String pushInvoice = (String)delegateExecution.getVariable("pushInvoice");
        String mainContract = (String)delegateExecution.getVariable("mainContract");

        Map<String, Object> variables = new HashMap<>();

        if("Revision".equals(mainContract)){
            variables.put("acceptPerformedJob", "invoiced");
            variables.put("monthActNumber", delegateExecution.getVariable("monthActNumber"));
            variables.put("invoiceNumber", delegateExecution.getVariable("invoiceNumber"));
            variables.put("invoiceDate", delegateExecution.getVariable("invoiceDate"));

            if(pushInvoice == null || "enable".equals(pushInvoice)){
                SpinJsonNode selectedRevisions = delegateExecution.<JsonValue>getVariableTyped("selectedRevisions").getValue();
                for(String revisionId: selectedRevisions.fieldNames()){
                    runtimeService.setVariables(revisionId, variables);
                }
            }
        } else if("Roll-out".equals(mainContract)) {
            String rolloutActType = (String) delegateExecution.getVariable("rolloutActType");
            if("RO-1".equals(rolloutActType)) {
                variables.put("invoiceRO1Number", delegateExecution.getVariable("invoiceNumber"));
                variables.put("invoiceRO1Date", delegateExecution.getVariable("invoiceDate"));
                variables.put("rolloutRO1", "true");
            } else if("RO-2".equals(rolloutActType)) {
                variables.put("invoiceRO2Number", delegateExecution.getVariable("invoiceNumber"));
                variables.put("invoiceRO2Date", delegateExecution.getVariable("invoiceDate"));
                variables.put("rolloutRO2", "true");
            } else if("RO-3".equals(rolloutActType)) {
                variables.put("invoiceRO3Number", delegateExecution.getVariable("invoiceNumber"));
                variables.put("invoiceRO3Date", delegateExecution.getVariable("invoiceDate"));
                variables.put("rolloutRO3", "true");
            } else if("RO-4".equals(rolloutActType)) {
                variables.put("invoiceRO4Number", delegateExecution.getVariable("invoiceNumber"));
                variables.put("invoiceRO4Date", delegateExecution.getVariable("invoiceDate"));
                variables.put("rolloutRO4", "true");
            }

            if(pushInvoice == null || "enable".equals(pushInvoice)){
                SpinJsonNode selectedRevisions = delegateExecution.<JsonValue>getVariableTyped("selectedRevisions").getValue();
                for(String revisionId: selectedRevisions.fieldNames()){
                    runtimeService.setVariables(revisionId, variables);

                    if("RO-1".equals(rolloutActType)){
                        String rolloutRO2 = (String) delegateExecution.getVariable("rolloutRO2");
                        String rolloutRO3 = (String) delegateExecution.getVariable("rolloutRO3");
                        if("true".equals(rolloutRO2) && "true".equals(rolloutRO3)){
                            runtimeService.setVariable(revisionId, "rolloutActToPass", "passed");
                        }
                    }
                    if("RO-2".equals(rolloutActType)){
                        String rolloutRO1 = (String) delegateExecution.getVariable("rolloutRO1");
                        String rolloutRO3 = (String) delegateExecution.getVariable("rolloutRO3");
                        if("true".equals(rolloutRO1) && "true".equals(rolloutRO3)){
                            runtimeService.setVariable(revisionId, "rolloutActToPass", "passed");
                        }
                    }
                    if("RO-3".equals(rolloutActType)){
                        String rolloutRO1 = (String) delegateExecution.getVariable("rolloutRO1");
                        String rolloutRO2 = (String) delegateExecution.getVariable("rolloutRO2");
                        if("true".equals(rolloutRO1) && "true".equals(rolloutRO2)){
                            runtimeService.setVariable(revisionId, "rolloutActToPass", "passed");
                        }
                    }
                    if("RO-4".equals(rolloutActType)){
                        runtimeService.setVariable(revisionId, "rolloutActToPass", "passed");
                    }
                }
            }
        }
    }

}
