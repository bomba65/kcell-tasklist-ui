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
            String rolloutActType = (String)delegateExecution.getVariable("rolloutActType");
            if("RO-1".equals(rolloutActType)) {
                variables.put("rolloutActToPass", "RO-2");
                variables.put("invoiceRO1Number", delegateExecution.getVariable("invoiceNumber"));
                variables.put("invoiceRO1Date", delegateExecution.getVariable("invoiceDate"));
            } else if("RO-2".equals(rolloutActType)) {
                variables.put("rolloutActToPass", "RO-3");
                variables.put("invoiceRO2Number", delegateExecution.getVariable("invoiceNumber"));
                variables.put("invoiceRO2Date", delegateExecution.getVariable("invoiceDate"));
            } else if("RO-3".equals(rolloutActType)) {
                variables.put("rolloutActToPass", "passed");
                variables.put("invoiceRO2Number", delegateExecution.getVariable("invoiceNumber"));
                variables.put("invoiceRO2Date", delegateExecution.getVariable("invoiceDate"));
            }

            if(pushInvoice == null || "enable".equals(pushInvoice)){
                SpinJsonNode selectedRevisions = delegateExecution.<JsonValue>getVariableTyped("selectedRevisions").getValue();
                for(String revisionId: selectedRevisions.fieldNames()){
                    runtimeService.setVariables(revisionId, variables);
                }
            }
        }
    }

}
