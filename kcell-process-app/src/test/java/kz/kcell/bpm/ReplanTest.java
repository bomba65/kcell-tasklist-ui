package kz.kcell.bpm;

import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@Deployment(resources = "replan-shared-site-address-plan.bpmn")
public class ReplanTest {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    @Test
    public void testNotNeededAfter_task_update_shared_site_address_plan(){
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("ReplanSharedSiteAddressPlan")
                .startAfterActivity("task_update_shared_site_address_plan")
                .setVariable("updateSiteSharingAddressPlanResult", "false")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("endevt_replan_not_needed").count();
        Assert.assertEquals(result, 1L);
    }

    @Test
    public void testUpdatedAfter_task_update_shared_site_address_plan(){
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("ReplanSharedSiteAddressPlan")
                .startAfterActivity("task_update_shared_site_address_plan")
                .setVariable("updateSiteSharingAddressPlanResult", "true")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("task_accept_or_reject_address_plan_modification").count();
        Assert.assertEquals(result, 1L);
    }

    @Test
    public void testRejectAfter_task_accept_or_reject_address_plan_modification(){
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("ReplanSharedSiteAddressPlan")
                .startAfterActivity("task_accept_or_reject_address_plan_modification")
                .setVariable("addressPlanUpdateAcceptedResult", "false")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("task_update_shared_site_address_plan").count();
        Assert.assertEquals(result, 1L);
    }

    @Test
    public void testAcceptAfter_task_accept_or_reject_address_plan_modification(){
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("ReplanSharedSiteAddressPlan")
                .startAfterActivity("task_accept_or_reject_address_plan_modification")
                .setVariable("addressPlanUpdateAcceptedResult", "true")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("task_update_address_plan_in_db").count();
        Assert.assertEquals(result, 1L);
    }
}
