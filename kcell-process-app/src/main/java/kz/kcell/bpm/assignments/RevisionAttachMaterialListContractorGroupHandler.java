package kz.kcell.bpm.assignments;

import org.apache.commons.lang.ArrayUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static kz.kcell.bpm.assignments.RevisionAttachMaterialListGroupHandler.ATTACH_MATERIAL_LIST_GROUP_TO_SITE;

public class RevisionAttachMaterialListContractorGroupHandler implements TaskListener {

    private static final Map<String, String> contractorsTitle =
            ((Supplier<Map<String, String>>) (() -> {
                Map<String, String> map = new HashMap<>();
                map.put("1", "avrora");
                map.put("2", "aicom");
                map.put("3", "spectr");
                map.put("4", "lse");
                map.put("5", "kcell");
                map.put("6", "alta");
                map.put("7", "logycom");
                map.put("8", "arlan");
                map.put("9", "inter");
                map.put("10","foresterhg");
                map.put("11","transtlc");
                return Collections.unmodifiableMap(map);
            })).get();

    @Override
    public void notify(DelegateTask delegateTask) {
        String contractor = delegateTask.getVariable("contractor").toString();
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String reason = delegateTask.getVariable("reason").toString();
        String mainContract = delegateTask.getVariable("mainContract").toString();
        if ("2022Work-agreement".equals(mainContract)) {
            String siteName = delegateTask.getVariable("site_name").toString();
            if(siteName.substring(0, 2).contains("99")){
                delegateTask.addCandidateGroup(siteRegion+"_contractor_"+contractorsTitle.get(contractor));
            }
            else {
                String group = ATTACH_MATERIAL_LIST_GROUP_TO_SITE.entrySet().stream()
                    .filter(e -> ArrayUtils.contains(e.getValue(), siteName.substring(0, 2)))
                    .findFirst()
                    .orElse(null)
                    .getKey();
                delegateTask.addCandidateGroup(group);
            }
        } else {
            if (contractor.equals("5")) {
                if (reason.equals("5")) {
                    delegateTask.addCandidateGroup(siteRegion + "_rollout");
                } else {
                    delegateTask.addCandidateGroup(siteRegion + "_engineer");
                }
            } else if (contractor.equals("4")) {
                delegateTask.addCandidateGroup(("nc".equals(siteRegion) ? "astana" : siteRegion) + "_contractor_" + contractorsTitle.get(contractor));
            } else {
                delegateTask.addCandidateGroup(siteRegion + "_contractor_" + contractorsTitle.get(contractor));
            }
        }
    }
}
