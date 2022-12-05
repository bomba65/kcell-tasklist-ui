package kz.kcell.bpm.revision;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.List;

@Log
public class CancelPrProcess implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {

        RuntimeService runtimeService = execution
            .getProcessEngineServices()
            .getRuntimeService();

        List<ProcessInstance> processInstances = runtimeService
            .createProcessInstanceQuery()
            .processDefinitionKey("CreatePR")
            .processInstanceBusinessKey("PR-" + execution.getProcessBusinessKey())
            .list();

        for(ProcessInstance processInstance: processInstances){
            runtimeService.setVariable(processInstance.getId(), "prCreationInProgress", "false");
        }
    }
}
