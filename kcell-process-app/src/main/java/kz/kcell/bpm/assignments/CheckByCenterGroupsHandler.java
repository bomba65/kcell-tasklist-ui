package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

import java.util.Arrays;

@lombok.extern.java.Log
public class CheckByCenterGroupsHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {

        String reason = delegateTask.getVariable("reason").toString();
        String mainContract = delegateTask.getVariable("mainContract").toString();

        if("2022Work-agreement".equals(mainContract)){
            if (reason.equals("4")) {
                delegateTask.addCandidateGroup("hq_operation_approve");
            } else if (Arrays.asList("1", "2", "3", "5","6").contains(reason)){
                delegateTask.addCandidateGroup("hq_development_approve");
            }
        }else {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup("'hq_optimization'");
            }else {
                delegateTask.addCandidateGroup("hq_transmission_engineer");
            }
        }
    }
}
