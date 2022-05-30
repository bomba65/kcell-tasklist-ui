package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

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
                return Collections.unmodifiableMap(map);
            })).get();

    @Override
    public void notify(DelegateTask delegateTask) {
        String contractor = delegateTask.getVariable("contractor").toString();
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String reason = delegateTask.getVariable("reason").toString();
        if(contractor.equals("5")){
            if(reason.equals("5")) {
                delegateTask.addCandidateGroup(siteRegion + "_rollout");
            } else {
                delegateTask.addCandidateGroup(siteRegion + "_engineer");
            }
        } else if(contractor.equals("4")) {
            delegateTask.addCandidateGroup(("nc".equals(siteRegion)?"astana":siteRegion) + "_contractor_" + contractorsTitle.get(contractor));
        } else {
            delegateTask.addCandidateGroup(siteRegion + "_contractor_" + contractorsTitle.get(contractor));
        }
    }
}
