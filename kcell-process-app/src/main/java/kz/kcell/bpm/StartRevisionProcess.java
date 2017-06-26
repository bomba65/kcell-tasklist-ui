package kz.kcell.bpm;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

public class StartRevisionProcess implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        execution.setVariable("isNewProcessCreated", "true");
        execution.setVariable("prCreationInProgress", "false");
    }
}
