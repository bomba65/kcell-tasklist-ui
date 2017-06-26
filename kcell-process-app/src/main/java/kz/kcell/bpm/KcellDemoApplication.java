package kz.kcell.bpm;

import org.camunda.bpm.application.PostDeploy;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

@ProcessApplication("Kcell Demo App")
public class KcellDemoApplication extends ServletProcessApplication {

    @Override
    public TaskListener getTaskListener() {
        return new TaskListener() {
            @Override
            public void notify(DelegateTask delegateTask) {

                /**
                 * Listen globally for task assignment events and send an email to assignee
                 */
                if (TaskListener.EVENTNAME_ASSIGNMENT.equals(delegateTask.getEventName())) {
                    new MailTaskAssigneeListener().notify(delegateTask);
                } else if (TaskListener.EVENTNAME_CREATE.equals(delegateTask.getEventName())) {
                    new MailTaskCandidatesListener().notify(delegateTask);
                }
            }
        };
    }

    @PostDeploy
    public void startFirstProcess(ProcessEngine processEngine) {
        this.createUsers(processEngine);
    }

    private void createUsers(ProcessEngine processEngine) {
        (new DemoDataGenerator()).createUsers(processEngine);
    }
}
