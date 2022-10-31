package kz.kcell.bpm.assignprnumbers;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.variable.value.StringValue;

@Log
public class ExistingPr implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String jrNumber = execution.<StringValue>getVariableTyped("jrNumber").getValue();
        String prNumber = execution.<StringValue>getVariableTyped("prNumber").getValue();
        log.info("jr Number = " + jrNumber + " pr number = " + prNumber);
        //TODO: implement asset REST api calls
    }
}
