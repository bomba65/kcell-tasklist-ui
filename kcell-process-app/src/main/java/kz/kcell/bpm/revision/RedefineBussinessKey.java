package kz.kcell.bpm.revision;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;

public class RedefineBussinessKey implements ExecutionListener {


    @Override
    public void notify(DelegateExecution execution) throws Exception {

        String jrNumber = (String) execution.getVariable("jrNumber");
        execution.setProcessBusinessKey(jrNumber);
    }
}
