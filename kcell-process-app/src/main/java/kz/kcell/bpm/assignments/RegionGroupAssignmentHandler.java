package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.*;

public class RegionGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        String siteRegion = delegateTask.getVariable("siteRegion").toString();

        Boolean createdAutomatically = delegateTask.hasVariable("createdAutomatically") && Boolean.valueOf(delegateTask.getVariable("createdAutomatically").toString());

        if (reason != null && !createdAutomatically) {
            if (reason.equals("2")) {
                delegateTask.addCandidateGroup(siteRegion + "_transmission_approve");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup(siteRegion + "_operation_approve");
            } else if (Arrays.asList("1", "3", "5", "6").contains(reason)){
                delegateTask.addCandidateGroup(siteRegion + "_development_approve");
            }
        }
    }
}
