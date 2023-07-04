package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class ApproveMaterialsListByMlApproversAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String siteRegion = String.valueOf(delegateTask.getVariable("siteRegion"));
        String reason = String.valueOf(delegateTask.getVariable("reason"));
        String mainContract = delegateTask.getVariable("mainContract").toString();

        if("2022Work-agreement".equals(mainContract)||"technical_maintenance_services".equals(mainContract)){
            if (reason.equals("4")) {
                delegateTask.addCandidateGroup(siteRegion + "_operation_mlapprove");
            } else if (Arrays.asList("1", "2", "3", "5","6").contains(reason)){
                delegateTask.addCandidateGroup(siteRegion + "_development_mlapprove");
            }
        }else {
            if (reason != null) {
                switch (reason) {
                    case "1":
                        delegateTask.addCandidateGroup(siteRegion + "_optimization_mlapprover");
                        break;
                    case "2":
                        delegateTask.addCandidateGroup(siteRegion + "_transmission_mlapprover");
                        break;
                    case "3":
                        delegateTask.addCandidateGroup(siteRegion + "_infrastructure_mlapprover");
                        break;
                    case "4":
                        delegateTask.addCandidateGroup(siteRegion + "_sao_mlapprover");
                        break;
                    case "5":
                        delegateTask.addCandidateGroup(siteRegion + "_rollout");
                        break;
                }
            }
        }
    }
}
