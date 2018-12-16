package kz.kcell.bpm.leasing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.camunda.bpm.engine.delegate.*;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;

public class TaskSetStatus implements TaskListener {

    Expression variable;
    Expression value;
    Expression label;

    @Override
    public void notify(DelegateTask delegateTask) {

        String variable = this.variable.getValue(delegateTask).toString();
        String value = this.value.getValue(delegateTask).toString();
        String label = this.label.getValue(delegateTask).toString();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode status = mapper.createObjectNode();
        status.put("value", value);
        status.put("label", label);

        JsonValue jsonValue = SpinValues.jsonValue(status.toString()).create();

        delegateTask.setVariable(variable, jsonValue);
    }
}
