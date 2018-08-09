package kz.kcell.flow.revision;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.Spin;
import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StatusListener implements ExecutionListener {

    Expression statusId;
    Expression statusName;
    Expression returnStatus;
    Expression parentStatus;

    @Override
    public void notify(DelegateExecution delegateExecution) {

        String statusId = this.statusId.getValue(delegateExecution).toString();
        String statusName = this.statusName.getValue(delegateExecution).toString();
        Boolean returnStatus = Boolean.valueOf(this.returnStatus.getValue(delegateExecution).toString());
        String parentStatus = this.parentStatus.getValue(delegateExecution).toString();

        String comment = "";
        if(returnStatus){
            SpinJsonNode resolutionArray = delegateExecution.<JsonValue>getVariableTyped("resolutions").getValue();

            SpinJsonNode lastResolution = resolutionArray.elements().get(resolutionArray.elements().size()-1);
            comment = lastResolution.prop("comment").stringValue();
        }

        JsonValue jsonValue = SpinValues.jsonValue("{\"statusId\" : \"" + statusId
            + "\",\"statusName\" : \"" + statusName
            + "\",\"comment\" : \"" + comment
            + "\",\"date\" : " + (new Date()).getTime()
            + ",\"returnStatus\" : " + returnStatus
            + ",\"parentStatus\" : \"" + parentStatus
            + "\"}").create();

        delegateExecution.setVariable("status", jsonValue);

        SpinList<SpinJsonNode> statusHistory = new SpinListImpl<>();
        if(delegateExecution.hasVariable("statusHistory")){
            statusHistory = delegateExecution.<JsonValue>getVariableTyped("statusHistory").getValue().elements();
        }
        statusHistory.add(jsonValue.getValue());

        delegateExecution.setVariable("statusHistory", SpinValues.jsonValue(statusHistory.toString()));
    }
}
