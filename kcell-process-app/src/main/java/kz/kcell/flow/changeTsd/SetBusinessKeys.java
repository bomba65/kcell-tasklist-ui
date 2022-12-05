package kz.kcell.flow.changeTsd;

import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Supplier;

@Log
@Service("setBusinessKeys")
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SetBusinessKeys implements JavaDelegate {

    @Override
    public void execute(DelegateExecution delegateExecution) {

        if(delegateExecution.getProcessBusinessKey()==null){
            JSONObject tsd = new JSONObject(delegateExecution.getVariable("selectedTsd").toString());
            String businessKey = tsd.getString("business_key");
            if(businessKey != null) {
                String proc_def_key = delegateExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateExecution.getProcessDefinitionId()).getKey();
                businessKey += "-" + proc_def_key;
                delegateExecution.setProcessBusinessKey(businessKey);
                delegateExecution.setVariable(proc_def_key + "Number", businessKey);
            }
        }
    }
}
