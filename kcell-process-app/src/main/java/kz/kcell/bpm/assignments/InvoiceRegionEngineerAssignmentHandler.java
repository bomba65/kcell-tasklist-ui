package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

public class InvoiceRegionEngineerAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String reason = delegateTask.getVariable("workType").toString();
        if (reason != null) {
            if (reason.equals("1")) {
                delegateTask.addCandidateGroup("alm_engineer_po_ma_approver");
            } else if (reason.equals("2")) {
                delegateTask.addCandidateGroup("alm_engineer_tr_ma_approver");
            } else if (reason.equals("3")) {
                delegateTask.addCandidateGroup("alm_engineer_sfm_ma_approver");
            } else if (reason.equals("4")) {
                delegateTask.addCandidateGroup("alm_engineer_sao_ma_approver");
            }
        }
    }
}
