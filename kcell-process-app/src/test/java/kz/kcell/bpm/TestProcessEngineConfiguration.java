package kz.kcell.bpm;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.behavior.UserTaskActivityBehavior;
import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.util.xml.Element;

import java.util.ArrayList;

public class TestProcessEngineConfiguration extends StandaloneInMemProcessEngineConfiguration {
    public TestProcessEngineConfiguration() {
        this.preParseListeners = new ArrayList<>();
        this.preParseListeners.add(new AbstractBpmnParseListener() {
            final TaskListener TASK_LISTENER = new MailTaskAssigneeListener();

            @Override
            public void parseUserTask(Element userTaskElement, ScopeImpl scope, ActivityImpl activity) {
                UserTaskActivityBehavior activityBehavior = (UserTaskActivityBehavior) activity.getActivityBehavior();
                TaskDefinition taskDefinition = activityBehavior.getTaskDefinition();
                addTaskCreateListeners(taskDefinition);
                addTaskAssignmentListeners(taskDefinition);
                addTaskCompleteListeners(taskDefinition);
                addTaskDeleteListeners(taskDefinition);
            }

            void addTaskAssignmentListeners(TaskDefinition taskDefinition) {
                taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, TASK_LISTENER);
            }

            void addTaskCreateListeners(TaskDefinition taskDefinition) {
                taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, TASK_LISTENER);
            }

            void addTaskCompleteListeners(TaskDefinition taskDefinition) {
                taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, TASK_LISTENER);
            }

            void addTaskDeleteListeners(TaskDefinition taskDefinition) {
                taskDefinition.addTaskListener(TaskListener.EVENTNAME_DELETE, TASK_LISTENER);
            }

        });
    }

}
