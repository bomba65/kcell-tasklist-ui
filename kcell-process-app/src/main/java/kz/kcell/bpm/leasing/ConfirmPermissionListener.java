package kz.kcell.bpm.leasing;

import org.apache.commons.lang.ArrayUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.variable.value.LongValue;

public class ConfirmPermissionListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String ncpID = delegateTask.getVariable("ncpID").toString();
        Long legalTypeCatalogId = delegateTask.<LongValue>getVariableTyped("legalTypeCatalogId").getValue();

        JSONObject candidate = new JSONObject(delegateTask.getVariable("candidate").toString());

        if (ncpID != null && ncpID.startsWith("11") && !siteRegion.equals("astana")) {
            siteRegion = "astana";
            delegateTask.setVariable("siteRegion", siteRegion);
        }
        if (candidate.has("constructionType") && candidate.getJSONObject("constructionType").getString("catalogsId").equals("18")
            && ArrayUtils.contains(new Long[]{5L, 6L, 7L}, legalTypeCatalogId)) {
            delegateTask.addCandidateGroup("hq_leasing");
        } else {
            delegateTask.addCandidateGroup(siteRegion + "_leasing");
        }
    }
}
