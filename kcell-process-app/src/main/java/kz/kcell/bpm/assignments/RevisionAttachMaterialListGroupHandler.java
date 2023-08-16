package kz.kcell.bpm.assignments;

import org.apache.commons.lang.ArrayUtils;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RevisionAttachMaterialListGroupHandler implements TaskListener {

    public final static Map<String, String[]> ATTACH_MATERIAL_LIST_GROUP_TO_SITE = new HashMap<String, String[]>() {{
        put("alm_contractor_alta", new String[] {"00", "01", "03", "04", "05", "06","07"});
        put("nc_contractor_alta", new String[] {"10","11", "12", "13"});
        put("south_contractor_alta", new String[] {"43", "44"});
        put("east_contractor_alta", new String[] {"33", "34", "35", "36"});
        put("nc_contractor_logycom", new String[] {"14", "15", "16", "17", "21", "22", "23", "24"});
        put("east_contractor_logycom", new String[] {"31", "32"});
        put("south_contractor_foresterhg", new String[] {"40","41", "42", "47","48"});
        put("west_contractor_foresterhg", new String[] {"51", "52", "61", "62", "81", "82"});
        put("south_contractor_arlan", new String[] {"45", "46"});
        put("west_contractor_transtlc", new String[] {"71", "72"});
    }};

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
        String mainContract = delegateTask.getVariable("mainContract").toString();
        String siteId = delegateTask.getVariable("siteName").toString();
        String oblastName = delegateTask.getVariable("oblastName").toString();

        if ("2022Work-agreement".equals(mainContract)) {
            String siteName = delegateTask.getVariable("site_name").toString();
            if(siteName.substring(0, 2).equals("07")&&siteRegion.equals("nc")){
                delegateTask.addCandidateGroup("nc_contractor_logycom");
            }
            else {
                String group = ATTACH_MATERIAL_LIST_GROUP_TO_SITE.entrySet().stream()
                    .filter(e -> ArrayUtils.contains(e.getValue(), siteName.substring(0, 2)))
                    .findFirst()
                    .orElse(null)
                    .getKey();
                delegateTask.addCandidateGroup(group);
            }
        } else if ("technical_maintenance_services".equals(mainContract)) {
            String siteIdFirstTwoDigits = siteId.substring(0, 2);
            if (Arrays.asList("00", "01", "04", "05", "06", "07").contains(siteIdFirstTwoDigits)&&("Алматинская область".equals(oblastName)||"Жамбылская область".equals(oblastName))) {
                delegateTask.addCandidateGroup("alm_contractor_arlan");
            } else if (Arrays.asList("04", "05", "06").contains(siteIdFirstTwoDigits)&&"Жетысуская область".equals(oblastName)) {
                delegateTask.addCandidateGroup("alm_contractor_inter");
            } else if ("07".equals(siteIdFirstTwoDigits)&&"Карагандинская область".equals(oblastName)) {
                delegateTask.addCandidateGroup("nc_contractor_logycom");
            } else if (Arrays.asList("10", "11", "12", "13", "14", "15", "16", "17", "21", "22", "23", "24").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("nc_contractor_logycom");
            } else if (Arrays.asList("31", "32", "33", "34", "35", "36").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("east_contractor_logycom");
            } else if (Arrays.asList("43", "44", "45", "46").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("south_contractor_arlan");
            } else if (Arrays.asList("40","41", "42", "47", "48").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("south_contractor_foresterhg");
            } else if (Arrays.asList("71", "72").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("west_contractor_logycom");
            } else if (Arrays.asList("51", "52", "61", "62", "81", "82").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("west_contractor_foresterhg");
            }

        } else if ("2023primary_source".equals(mainContract)) {
            String siteIdFirstTwoDigits = siteId.substring(0, 2);
            if (Arrays.asList("03", "05", "07").contains(siteIdFirstTwoDigits)&&"Жамбылская область".equals(oblastName)) {
                delegateTask.addCandidateGroup("alm_contractor_arlan");
            } else if (Arrays.asList("00", "01", "04", "06").contains(siteIdFirstTwoDigits) || Arrays.asList("03", "05", "07").contains(siteIdFirstTwoDigits)&&"Алматинская область".equals(oblastName)) {
                delegateTask.addCandidateGroup("alm_contractor_inter");
            } else if (Arrays.asList("40", "41", "42", "47", "48").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("south_contractor_inter");
            } else if (Arrays.asList("71", "72").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("west_contractor_inter");
            } else if (Arrays.asList("10", "11", "12", "13").contains(siteIdFirstTwoDigits) || siteIdFirstTwoDigits.equals("07") && "Карагандинская область".equals(oblastName)) {
                delegateTask.addCandidateGroup("nc_contractor_logycom");
            } else if (Arrays.asList("43", "44", "45", "46").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("south_contractor_arlan");
            } else if (Arrays.asList("61", "62").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("west_contractor_logycom");
            } else if (Arrays.asList("51", "52", "61", "62", "81", "82").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup("west_contractor_foresterhg");
            } else if (Arrays.asList("99").contains(siteIdFirstTwoDigits)) {
                delegateTask.addCandidateGroup(siteRegion + "_contractor_" + contractorsTitle.get(contractor));
            }

        } else {
            delegateTask.addCandidateGroup(siteRegion + "_engineer");
        }
    }
}
