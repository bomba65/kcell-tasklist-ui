package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

@lombok.extern.java.Log
public class ValidateTrByCenterByGroupHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String group = String.valueOf(delegateTask.getVariable("group"));
        String reason = delegateTask.getVariable("reason").toString();
        String mainContract = delegateTask.getVariable("mainContract").toString();

        if(Arrays.asList("2022Work-agreement","technical_maintenance_services","2023primary_source").contains(mainContract)){
            if (Arrays.asList("2", "4").contains(reason) && group.equals("\"Development\"")) {
                delegateTask.addCandidateGroup("hq_development_tr");
            } else if (Arrays.asList("1", "2", "3", "5","6").contains(reason) && group.equals("\"Operation\"")) {
                delegateTask.addCandidateGroup("hq_operation_tr");
            } else if (Arrays.asList("1", "3", "4", "5").contains(reason) && group.equals("\"Transmission\"")){
                delegateTask.addCandidateGroup("hq_transmission_tr");
            }
        }else {
            if (group != null) {
                switch (group) {
                    case "\"P&O\"":
                        delegateTask.addCandidateGroup("hq_optimization");
                        break;
                    case "\"Transmission\"":
                        delegateTask.addCandidateGroup("hq_transmission_engineer");
                        break;
                    case "\"S&FM\"":
                        delegateTask.addCandidateGroup("hq_infrastructure");
                        break;
                    case "\"Operation\"":
                        delegateTask.addCandidateGroup("hq_operation");
                        break;
                    case "\"Roll-out\"":
                        delegateTask.addCandidateGroup("hq_rollout");
                        break;
                }
            }
        }
    }
}

