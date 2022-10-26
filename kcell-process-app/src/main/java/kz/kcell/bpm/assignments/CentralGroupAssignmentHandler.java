package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class CentralGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        String mainContract = delegateTask.getVariable("mainContract").toString();

        if (reason != null) {
            if("2022Work-agreement".equals(mainContract)){
                if (reason.equals("4")) {
                    delegateTask.addCandidateGroup("hq_operation_approve");
                } else if (Arrays.asList("1", "2", "3", "5").contains(reason)){
                    delegateTask.addCandidateGroup("hq_development_approve");
                }
            }else {
                switch (reason) {
                    case "1":
                        delegateTask.addCandidateGroup("hq_optimization");
                        break;
                    case "2":
                        //Данные работы идут по другой ветке
                        break;
                    case "3":
                        delegateTask.addCandidateGroup("hq_infrastructure");
                        break;
                    case "4":
                        delegateTask.addCandidateGroup("hq_operation");
                        break;
                    case "5":
                        delegateTask.addCandidateGroup("hq_rollout");
                        break;
                }
            }
        }
    }
}
