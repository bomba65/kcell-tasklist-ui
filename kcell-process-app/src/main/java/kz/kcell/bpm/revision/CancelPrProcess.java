package kz.kcell.bpm.revision;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.runtime.Execution;

import java.util.List;

public class CancelPrProcess implements ExecutionListener {


    @Override
    public void notify(DelegateExecution execution) throws Exception {

        List<Execution> processInstances = execution
            .getProcessEngineServices()
            .getRuntimeService()
            .createExecutionQuery()
            .processDefinitionKey("CreatePR")
            .processInstanceBusinessKey("PR-" + execution.getProcessBusinessKey())
            .list();

        for(Execution prExecution: processInstances){
            execution
                .getProcessEngineServices()
                .getRuntimeService().setVariable(prExecution.getProcessInstanceId(), "prCreationInProgress", false);
        }
    }
}
