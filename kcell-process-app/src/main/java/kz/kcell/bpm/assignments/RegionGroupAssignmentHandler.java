package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RegionGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup(siteRegion + "_optimization_head");
            } else if (reason.equals("2")) {
                delegateTask.addCandidateGroup(siteRegion + "_transmission_head");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup(siteRegion + "_infrastructure_head");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup(siteRegion + "_operation_head");
            }
        }
    }
}