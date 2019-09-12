package kz.kcell.bpm.assignments.dismantleReplace;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class CentralGroupsAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String requestType = (String) delegateTask.getVariable("requestType");

        if("dismantle".equals(requestType)){
            delegateTask.setDescription("SITE DISMANTLING REQUEST");
        } else if("replacement".equals(requestType)){
            delegateTask.setDescription("SITE REPLACEMENT REQUEST");
        }

        String group = String.valueOf(delegateTask.getVariable("group"));

        if (group != null) {
            if (group.equals("\"Central Leasing Unit\"")) {
                delegateTask.addCandidateGroup("dismantle_replacement_central_leasing");
            } else if (group.equals("\"Central Transmission Unit\"")) {
                delegateTask.addCandidateGroup("dismantle_replacement_central_tnu");
            } else if (group.equals("\"Central S&FM Unit\"")) {
                delegateTask.addCandidateGroup("dismantle_replacement_central_sfm");
            } else if (group.equals("\"Central Planning Unit\"")) {
                delegateTask.addCandidateGroup("dismantle_replacement_central_planning");
            }else if (group.equals("\"Central SAO Unit\"")) {
                delegateTask.addCandidateGroup("dismantle_replacement_central_sao");
            }
        }
    }
}
