package kz.kcell.bpm.assignments;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;

@lombok.extern.java.Log
public class RegionWarehouseAssignmentHandler implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {


        String warehouse = String.valueOf(delegateTask.getVariable("warehouse"));

        if (warehouse != null) {
            if (warehouse.equals("\"alm\"")) {
                delegateTask.addCandidateGroup("warehouse_alm");
            } else if (warehouse.equals("\"astana\"")) {
                delegateTask.addCandidateGroup("warehouse_astana");
            } else if (warehouse.equals("\"atyrau\"")) {
                delegateTask.addCandidateGroup("warehouse_atyrau");
            } else if (warehouse.equals("\"aktau\"")) {
                delegateTask.addCandidateGroup("warehouse_aktau");
            } else if (warehouse.equals("\"aktobe\"")) {
                delegateTask.addCandidateGroup("warehouse_aktobe");
            }
        }
    }
}
