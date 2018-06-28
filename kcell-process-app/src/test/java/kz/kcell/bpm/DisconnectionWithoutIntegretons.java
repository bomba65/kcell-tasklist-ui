package kz.kcell.bpm;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@Deployment(resources = "Disconnection.bpmn")
public class DisconnectionWithoutIntegretons {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Test
    public void disconnectionProcess1(){
    	//Map<String, Object> user = new HashMap<>();
    	//user.put("name", "Dauren");
    	ProcessInstanceWithVariables pi = processEngineRule.getRuntimeService().createProcessInstanceByKey("disconnectionProcesss").executeWithVariablesInReturn();
    	Task task = processEngineRule.getTaskService().createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();
    	//task.getFormKey()
    	//processEngineRule.getTaskService().complete(task.getId(), Variables.createVariables().putValue("name", "Daur"));
    	
    	
    }
}
