package kz.kcell.bpm;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SetInvoicingStatus implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        RuntimeService runtimeService = delegateExecution.getProcessEngineServices().getRuntimeService();

        String pushInvoice = (String)delegateExecution.getVariable("pushInvoice");

        Map<String, Object> variables = new HashMap<>();
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
    }

}
