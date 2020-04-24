package kz.kcell.bpm.leasing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;

public class VariableSetStatus implements ExecutionListener {

    Expression variable;
    Expression value;

    @Override
    public void notify(DelegateExecution delegateExecution) {

        String variable = this.variable.getValue(delegateExecution).toString();
        String value = this.value.getValue(delegateExecution).toString();

        delegateExecution.setVariable(variable, value);
    }
}
