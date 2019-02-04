package kz.kcell.bpm.revision;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class DeleteAcceptanceDate implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        delegateTask.removeVariable("acceptanceDate");
    }
}
