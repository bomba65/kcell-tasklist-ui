package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RegionGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateUsers(Arrays.asList("Kerey.Zatilda@kcell.kz", "Kanat.Kulmukhambetov@kcell.kz"));
            } else if (reason.equals("2")) {
                delegateTask.addCandidateUsers(Arrays.asList("Maulen.Kempirbayev@kcell.kz"));
            } else if (reason.equals("3")) {
                delegateTask.addCandidateUsers(Arrays.asList("Kali.Esimbekov@kcell.kz", "Maulen.Kempirbayev@kcell.kz", "Samat.Akhmetov@kcell.kz", "Zhanat.Seitkanov@kcell.kz"));
            } else if (reason.equals("4")) {
                delegateTask.addCandidateUsers(Arrays.asList("Kali.Esimbekov@kcell.kz"));
            }
        }
    }
}
