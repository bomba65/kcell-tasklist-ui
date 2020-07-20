package kz.kcell.bpm.leasing;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;

import java.util.Date;

public class VariableSetStatus implements ExecutionListener {

    Expression variable;
    Expression value;

    @Override
    public void notify(DelegateExecution delegateExecution) {

        String variable = this.variable.getValue(delegateExecution).toString();
        String value = this.value.getValue(delegateExecution).toString();

        if (variable.equals("generalStatus") || variable.equals("installationStatus")) {
            delegateExecution.setVariable(variable + "UpdatedDate", new Date());
        }

        delegateExecution.setVariable(variable, "null".equals(value)?"":value);
    }
}
