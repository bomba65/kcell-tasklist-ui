package kz.kcell.bpm.ma;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class MaSetReasonValues implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        if("ma_rollout".equals(delegateTask.getTaskDefinitionKey())){

        } else if("ma_po".equals(delegateTask.getTaskDefinitionKey())){

        } else if("ma_tnu1".equals(delegateTask.getTaskDefinitionKey())){

        } else if("ma_tnu2".equals(delegateTask.getTaskDefinitionKey())){

        } else if("ma_tnu3".equals(delegateTask.getTaskDefinitionKey())){

        } else if("ma_sfm".equals(delegateTask.getTaskDefinitionKey())){

        } else if("ma_sao".equals(delegateTask.getTaskDefinitionKey())){

        }
    }
}
