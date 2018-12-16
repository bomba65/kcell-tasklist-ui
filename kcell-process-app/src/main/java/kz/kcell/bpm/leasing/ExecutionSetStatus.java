package kz.kcell.bpm.leasing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;

public class ExecutionSetStatus implements ExecutionListener {

    Expression variable;
    Expression value;
    Expression label;

    @Override
    public void notify(DelegateExecution delegateExecution) {

        String variable = this.variable.getValue(delegateExecution).toString();
        String value = this.value.getValue(delegateExecution).toString();
        Boolean label = Boolean.valueOf(this.label.getValue(delegateExecution).toString());

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode status = mapper.createObjectNode();
        status.put("value", value);
        status.put("label", label);

        JsonValue jsonValue = SpinValues.jsonValue(status.toString()).create();

        delegateExecution.setVariable(variable, jsonValue);
    }
}
