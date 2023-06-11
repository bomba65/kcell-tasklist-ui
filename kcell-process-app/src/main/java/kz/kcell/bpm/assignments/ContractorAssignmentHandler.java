package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ContractorAssignmentHandler implements TaskListener {

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
        String siteId = delegateTask.getVariable("siteName").toString();
        if ("2022Work-agreement".equals(mainContract) || "technical_maintenance_services".equals(mainContract)) {
            String siteIdFirstTwoDigits = siteId.substring(0, 2);
            if("99".contains(siteIdFirstTwoDigits)){
                delegateTask.addCandidateGroup(siteRegion+"_contractor_"+contractorsTitle.get(contractor));
            } else if ("07".equals(siteIdFirstTwoDigits)) {
                if ("technical_maintenance_services".equals(mainContract)) {
                    if (contractor.equals("7")) {
                        delegateTask.addCandidateGroup("nc_contractor_logycom");
                    } else if (contractor.equals("8")) {
                        delegateTask.addCandidateGroup("alm_contractor_arlan");
                    }
                } else if (siteRegion.equals("nc")) {
                    delegateTask.addCandidateGroup("nc_contractor_logycom");
                }
            } else if ("03".contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("alm_contractor_arlan");
            } else if (Arrays.asList("00", "01", "04", "05", "06", "07").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("alm_contractor_alta");
            } else if (Arrays.asList("10", "11", "12", "13").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("nc_contractor_alta");
            } else if (Arrays.asList("14", "15", "16", "17", "21", "22", "23", "24").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("nc_contractor_logycom");
            } else if (Arrays.asList("31", "32").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("east_contractor_logycom");
            } else if (Arrays.asList("33", "34", "35", "36").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("east_contractor_alta");
            } else if (Arrays.asList("40","41", "42", "47","48").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("south_contractor_foresterhg");
            } else if (Arrays.asList("43", "44").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("south_contractor_alta");
            } else if (Arrays.asList("45", "46").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("south_contractor_arlan");
            } else if (Arrays.asList("51", "52", "61", "62", "81", "82").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("west_contractor_foresterhg");
            } else if (Arrays.asList("71", "72").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("west_contractor_transtlc");
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
