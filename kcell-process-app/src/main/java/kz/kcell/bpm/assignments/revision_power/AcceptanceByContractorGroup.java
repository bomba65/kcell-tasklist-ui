package kz.kcell.bpm.assignments.revision_power;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;

public class AcceptanceByContractorGroup implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        JacksonJsonNode contract= (JacksonJsonNode) delegateTask.getVariable("contract");
       String id= String.valueOf(contract.prop("id"));
       if(id.contains("99377"))delegateTask.addCandidateGroup("power_contractor_arcommm");
       else if(id.contains("99412"))delegateTask.addCandidateGroup("power_contractor_alfa");
       else if(id.contains("758437"))delegateTask.addCandidateGroup("power_contractor_alta");
       else delegateTask.addCandidateGroup("power_contractor_innstroy");
    }
}
