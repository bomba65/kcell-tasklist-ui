package kz.kcell.bpm;

import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineTestCase;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class KcellDemoApplicationTest extends ProcessEngineTestCase {

    // TODO: Set the Demo User Email Address
    private static final String EMAIL = "demo@example.org";


    @Deployment(resources = "revision.bpmn")
    public void testSimpleProcess() {
        /*Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("city", "CITY_5164");

        // Create the user that will be informed on assignment
        User newUser = identityService.newUser("demo");
        newUser.setEmail(EMAIL);
        identityService.saveUser(newUser);

        runtimeService.startProcessInstanceByKey("KcellDemoProcess", variables);

        Task task = taskService.createTaskQuery().singleResult();
        Assert.assertNotNull(task);

        int temperature = Integer.parseInt( taskService.getVariable(task.getId(), "temperature").toString() );
        if(temperature >= 18) {
            Assert.assertEquals("PackWarmTask", task.getTaskDefinitionKey());
        } else {
            Assert.assertEquals("PackColdTask", task.getTaskDefinitionKey());
        }*/

    }

}
