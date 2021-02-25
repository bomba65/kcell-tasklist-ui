package kz.kcell.bpm.leasing;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONObject;

public class ConfirmPermissionListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String ncpID = delegateTask.getVariable("ncpID").toString();

        JSONObject candidate = new JSONObject(delegateTask.getVariable("candidate").toString());

        if (ncpID != null && ncpID.startsWith("11") && !siteRegion.equals("astana")) {
            siteRegion = "astana";
            delegateTask.setVariable("siteRegion", siteRegion);
        }
        if (candidate.has("constructionType") && candidate.getJSONObject("constructionType").getString("id").equals("141")) {
            delegateTask.addCandidateGroup("hq_leasing");
        } else {
            delegateTask.addCandidateGroup(siteRegion + "_leasing");
        }
    }
}
