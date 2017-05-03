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


    @Deployment(resources = "test-mail.bpmn")
    public void testSimpleProcess() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("starter", "yernaz.kalingarayev");

        // Create the user that will be informed on assignment
        User newUser = identityService.newUser("yernaz.kalingarayev");
        newUser.setEmail("yernaz.kalingarayev@kcell.kz");
        identityService.saveUser(newUser);
        identityService.setAuthenticatedUserId("demo");

        runtimeService.startProcessInstanceByKey("Test_Mail", variables);

//        Task task = taskService.createTaskQuery().singleResult();
//        Assert.assertNotNull(task);

    }

}
