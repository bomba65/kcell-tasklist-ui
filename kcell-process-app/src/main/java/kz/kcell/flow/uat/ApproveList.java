package kz.kcell.flow.uat;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ApproveList implements TaskListener {

    Expression manual;
    Expression manualRole;
    Expression result;
    Expression comment;
    Expression role;

    @Override
    public void notify(DelegateTask delegateTask)  {

        Boolean manual = Boolean.valueOf(this.manual.getValue(delegateTask).toString());
        String manualRole = this.manualRole.getValue(delegateTask).toString();
        String result = this.result.getValue(delegateTask).toString();
        String comment = this.comment.getValue(delegateTask).toString();
        String role = this.role.getValue(delegateTask).toString();

        delegateTask.getAssignee();
        Date date = new Date();


        if (manual) {
            role = delegateTask.getVariable(manualRole).toString();
        }

        JsonValue jsonValue = SpinValues.jsonValue("{\"role\" : \"" + role
            + "\",\"responsible\" : \"" + delegateTask.getAssignee()
            + "\",\"comment\" : \"" + delegateTask.getVariable(comment)
            + "\",\"date\" : \"" + (new Date()).getTime()
            + "\",\"result\" : \"" + delegateTask.getVariable(result)
            + "\"}").create();

        SpinList<SpinJsonNode> approvalList = new SpinListImpl<>();
        if(delegateTask.hasVariable("approvalList")){
            approvalList = delegateTask.<JsonValue>getVariableTyped("approvalList").getValue().elements();
        }
        approvalList.add(jsonValue.getValue());

        delegateTask.setVariable("approvalList", SpinValues.jsonValue(approvalList.toString()));
    }
}

