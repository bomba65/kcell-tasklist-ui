package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class CentralGroupAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateUsers(Arrays.asList("Marina.Paramonova@kcell.kz", "Daniyar.Yespayev@kcell.kz", "Vladimir.Grachyov@kcell.kz"));
            } else if (reason.equals("2")) {
                delegateTask.addCandidateUsers(Arrays.asList("Sergey.Grigor@kcell.kz", "Galym.Tulenbayev@kcell.kz"));
            } else if (reason.equals("3")) {
                delegateTask.addCandidateUsers(Arrays.asList("Andrey.Medvedev@kcell.kz", "Sergey.Chumachenko@kcell.kz"));
            } else if (reason.equals("4")) {
                delegateTask.addCandidateUsers(Arrays.asList("Kairat.Parmanov@kcell.kz"));
            }
        }
    }
}
