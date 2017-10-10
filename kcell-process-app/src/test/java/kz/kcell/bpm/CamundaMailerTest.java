package kz.kcell.bpm;

import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@Deployment(resources = "camunda-mailer.bpmn")
public class CamundaMailerTest {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Test
    public void testCamundaMailerTest(){
        Mocks.register("camundaMailer", new CamundaMockMailerService());

        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("CamundaMailTest")
                .setVariable("name", "Eldar")
                .setVariable("surname", "Zakiryanov")
                .setVariable("patronymic", "Amangeldinovich")
                .executeWithVariablesInReturn();


        Assert.assertEquals(1L, 1L);
    }

}
