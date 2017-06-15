package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

public class UpdatePRStatusBySapSpecialistAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("reason").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateUsers(Arrays.asList("Gulzhan.Imandosova@kcell.kz", "Ernat.Suleimenov@kcell.kz", "Dana.Sabitova@kcell.kz"));
            } else if (reason.equals("2")) {
                delegateTask.addCandidateUsers(Arrays.asList("Aigerim.Satybekova@kcell.kz", "Tatyana.Solovyova@kcell.kz", "Bolat.Idirisov@kcell.kz", "Lyudmila.Vilkova@kcell.kz"));
            } else if (reason.equals("3")) {
                delegateTask.addCandidateUsers(Arrays.asList("Aigerim.Segizbayeva@kcell.kz", "Ernat.Suleimenov@kcell.kz"));
            } else if (reason.equals("4")) {
                delegateTask.addCandidateUsers(Arrays.asList("Chingis.Zholdassov@kcell.kz", "Keremet.Ibragimova@kcell.kz"));
            }
        }
    }
}
