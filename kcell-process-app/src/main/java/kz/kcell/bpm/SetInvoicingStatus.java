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
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class SetInvoicingStatus implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        RuntimeService runtimeService = delegateExecution.getProcessEngineServices().getRuntimeService();

        List<String> revisions = new ArrayList<>();

        String pushInvoice = (String)delegateExecution.getVariable("pushInvoice");

        if(pushInvoice == null || "enable".equals(pushInvoice)){
            SpinJsonNode selectedWorks = delegateExecution.<JsonValue>getVariableTyped("selectedWorks").getValue();
            for(String field: selectedWorks.fieldNames()){
                if(selectedWorks.prop(field).isArray()){
                    SpinList<SpinJsonNode> requests = selectedWorks.prop(field).elements();
                    requests.forEach(request -> {
                        String revisionId = request.prop("processInstanceId").stringValue();

                        if(!revisions.contains(revisionId)){
                            runtimeService.setVariable(revisionId, "acceptPerformedJob", "invoiced");
                            revisions.add(revisionId);
                        }
                    });
                }
            }
        }
    }

}
