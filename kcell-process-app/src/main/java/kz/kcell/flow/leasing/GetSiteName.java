package kz.kcell.flow.leasing;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("getSiteName")
public class GetSiteName implements JavaDelegate {

    private static final String BUSINESS_KEY = "businessKey";
    private static final String SITE_NAME = "siteName";

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String businessKey = delegateExecution.getVariable(BUSINESS_KEY) != null ? delegateExecution.getVariable(BUSINESS_KEY).toString() : null;
        String siteName = "";

        if (businessKey != null) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(businessKey).singleResult();
            if (processInstance != null) {
                String processInstanceId = processInstance.getProcessInstanceId();
                VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().processInstanceIdIn(processInstanceId).variableName(SITE_NAME).singleResult();
                if (variableInstance != null) {
                    siteName = variableInstance.getValue().toString();
                }
            }
        }

        delegateExecution.setVariable(SITE_NAME, siteName);
    }
}
