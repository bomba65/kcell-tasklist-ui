package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class RegionHqLeasingAssignmentHandler implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String siteObjectType = delegateTask.getVariable("siteObjectType").toString();
        JSONObject candidate = new JSONObject(delegateTask.getVariable("candidate").toString());

        if (candidate.has("constructionType") && candidate.getJSONObject("constructionType").getString("id").equals("141")) {
            delegateTask.addCandidateGroup("hq_leasing");
        } else {
            if (siteObjectType.equals("national")) {
                delegateTask.addCandidateGroup("hq_leasing");
            } else {
                delegateTask.addCandidateGroup(siteRegion + "_leasing");
            }
        }
    }
}
