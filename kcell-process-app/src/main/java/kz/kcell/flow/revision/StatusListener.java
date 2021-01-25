package kz.kcell.flow.revision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.util.Calendar;
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

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode status = mapper.createObjectNode();
        status.put("statusId", statusId);
        status.put("statusName", statusName);
        status.put("comment", comment);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 6);
        status.put("date", calendar.getTime().getTime());
        status.put("returnStatus", returnStatus);
        status.put("parentStatus", parentStatus);

        JsonValue jsonValue = SpinValues.jsonValue(status.toString()).create();

        delegateExecution.setVariable("status", jsonValue);

        SpinList<SpinJsonNode> statusHistory = new SpinListImpl<>();
        if(delegateExecution.hasVariable("statusHistory")){
            statusHistory = delegateExecution.<JsonValue>getVariableTyped("statusHistory").getValue().elements();
        }
        statusHistory.add(jsonValue.getValue());

        delegateExecution.setVariable("statusHistory", SpinValues.jsonValue(statusHistory.toString()));
    }
}
