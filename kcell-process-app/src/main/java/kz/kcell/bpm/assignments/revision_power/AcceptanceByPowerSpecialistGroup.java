package kz.kcell.bpm.assignments.revision_power;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;

public class AcceptanceByPowerSpecialistGroup implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        if(siteRegion.contains("alm")) {
            delegateTask.addCandidateGroup( "power_approve_alm");
        } else if(siteRegion.contains("astana")) {
            delegateTask.addCandidateGroup("power_approve_astana");
        } else if(siteRegion.contains("nc")) {
            delegateTask.addCandidateGroup("power_approve_nc");
        } else if(siteRegion.contains("south")) {
            delegateTask.addCandidateGroup( "power_approve_south");
        } else if(siteRegion.contains("east")) {
            delegateTask.addCandidateGroup( "power_approve_east");
        } else if(siteRegion.contains("west")) {
            delegateTask.addCandidateGroup( "power_approve_west");
        }
    }
}
