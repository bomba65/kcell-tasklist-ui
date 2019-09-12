package kz.kcell.bpm.assignments.dismantleReplace;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class RegionHeadAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String requestType = (String) delegateTask.getVariable("requestType");
        String siteRegion = delegateTask.getVariable("siteRegion").toString();

        if("dismantle".equals(requestType)){
            String dismantlingInitiator = (String) delegateTask.getVariable("dismantlingInitiator");
            delegateTask.addCandidateGroup(siteRegion + "_" + dismantlingInitiator + "_head");
            delegateTask.setDescription("SITE DISMANTLING REQUEST");
        } else if("replacement".equals(requestType)){
            String replacementInitiator = (String) delegateTask.getVariable("replacementInitiator");
            delegateTask.addCandidateGroup(siteRegion + "_" + replacementInitiator + "_head");
            delegateTask.setDescription("SITE REPLACEMENT REQUEST");
        }
    }
}
