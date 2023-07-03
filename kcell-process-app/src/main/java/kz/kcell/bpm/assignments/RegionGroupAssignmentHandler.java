package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.*;

public class RegionGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String mainContract = delegateTask.getVariable("mainContract").toString();

        Boolean createdAutomatically = delegateTask.hasVariable("createdAutomatically") && Boolean.valueOf(delegateTask.getVariable("createdAutomatically").toString());

        if (reason != null && !createdAutomatically) {
            if("2022Work-agreement".equals(mainContract)||"technical_maintenance_services".equals(mainContract)){
                if (reason.equals("4")) {
                    delegateTask.addCandidateGroup(siteRegion + "_operation_approve");
                } else if (Arrays.asList("1", "2", "3", "5", "6").contains(reason)){
                    delegateTask.addCandidateGroup(siteRegion + "_development_approve");
                }
            }else {
                switch (reason) {
                    case "1":
                        delegateTask.addCandidateGroup(siteRegion + "_optimization_head");
                        break;
                    case "2":
                        delegateTask.addCandidateGroup(siteRegion + "_transmission_head");
                        break;
                    case "3":
                        delegateTask.addCandidateGroup(siteRegion + "_infrastructure_head");
                        break;
                    case "4":
                        delegateTask.addCandidateGroup(siteRegion + "_operation_head");
                        break;
                    case "5":
                        delegateTask.addCandidateGroup(siteRegion + "_rollout_head");
                        break;
                }
            }

        }
    }
}
