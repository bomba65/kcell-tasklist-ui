package kz.kcell.bpm.assignments;

import org.apache.commons.lang.ArrayUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.HashMap;
import java.util.Map;

public class RevisionAttachMaterialListGroupHandler implements TaskListener {

    public final static Map<String, String[]> ATTACH_MATERIAL_LIST_GROUP_TO_SITE = new HashMap<String, String[]>() {{
        put("alm_contractor_alta", new String[] {"00", "01", "03", "04", "05", "06","07"});
        put("nc_contractor_alta", new String[] {"11", "12", "13"});
        put("south_contractor_alta", new String[] {"43", "44"});
        put("east_contractor_alta", new String[] {"33", "34", "35", "36"});
        put("nc_contractor_logycom", new String[] {"14", "15", "16", "17", "21", "22", "23", "24"});
        put("east_contractor_logycom", new String[] {"31", "32"});
        put("south_contractor_foresterhg", new String[] {"41", "42", "47"});
        put("west_contractor_foresterhg", new String[] {"51", "52", "61", "62", "81", "82"});
        put("south_contractor_arlan", new String[] {"45", "46"});
        put("west_contractor_transtlc", new String[] {"71", "72"});
    }};

    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String mainContract = delegateTask.getVariable("mainContract").toString();

        if ("2022Work-agreement".equals(mainContract)) {
            String siteName = delegateTask.getVariable("site_name").toString();
            String group = ATTACH_MATERIAL_LIST_GROUP_TO_SITE.entrySet().stream()
                .filter(e -> ArrayUtils.contains(e.getValue(), siteName.substring(0, 2)))
                .findFirst()
                .orElse(null)
                .getKey();
            delegateTask.addCandidateGroup(group);
        } else {
            delegateTask.addCandidateGroup(siteRegion + "_engineer");
        }
    }
}
