package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class UploadTrByInitiatorGroupHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String siteRegion = delegateTask.getVariable("siteRegion").toString();
        String reason = delegateTask.getVariable("reason").toString();
        String mainContract = delegateTask.getVariable("mainContract").toString();

        if("2022Work-agreement".equals(mainContract)||"technical_maintenance_services".equals(mainContract)){
            if (reason.equals("4")) {
                delegateTask.addCandidateGroup(siteRegion + "_operation_tr");
            } else if (Arrays.asList("1", "2", "3", "5","6").contains(reason)){
                delegateTask.addCandidateGroup(siteRegion + "_development_tr");
            }
        }else {
            delegateTask.addCandidateGroup(siteRegion + "_engineer");
        }
    }
}
