package kz.kcell.bpm;

import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

@Deployment(resources = "kcell-host-beeline-site.bpmn")
public class KcellHostBeelineSiteReplanTest {

    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    //@Test
    public void testReplanAfter_do_initial_leasing_and_grant_site_access_1() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("do_initial_leasing_and_grant_site_access_1")
                .setVariable("replanStatus", "replan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("EndEvent_0enw9a6").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testNotReplanAfter_do_initial_leasing_and_grant_site_access_1() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("do_initial_leasing_and_grant_site_access_1")
                .setVariable("replanStatus", "notreplan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("prepare_project_plan").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testReplanAfter_prepare_project_plan() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("prepare_project_plan")
                .setVariable("replanStatus", "replan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("EndEvent_0szx0ip").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testNotReplanAfter_prepare_project_plan() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("prepare_project_plan")
                .setVariable("replanStatus", "notreplan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("ParallelGateway_1gmf0pg").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testReplanAfter_ExclusiveGateway_0otsvzm() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("ExclusiveGateway_0otsvzm")
                .setVariable("replanStatus", "replan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("EndEvent_0clumvv").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testNotReplanAfter_ExclusiveGateway_0otsvzm() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("ExclusiveGateway_0otsvzm")
                .setVariable("replanStatus", "notreplan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("do_leasing_procedures_before_works_start").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testReplanAfter_do_leasing_procedures_before_works_start() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("do_leasing_procedures_before_works_start")
                .setVariable("replanStatus", "replan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("EndEvent_06xrk5t").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testNotReplanAfter_do_leasing_procedures_before_works_start() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("do_leasing_procedures_before_works_start")
                .setVariable("replanStatus", "notreplan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("ParallelGateway_0fkygl4").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testReplanAfter_set_civil_works_done_status() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("set_civil_works_done_status")
                .setVariable("replanStatus", "replan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("EndEvent_0mcequj").count();
        Assert.assertEquals(result, 1L);
    }

    //@Test
    public void testNotReplanAfter_set_civil_works_done_status() {
        ProcessInstanceWithVariables processInstanceWithVariables = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("KcellHostBeelineSite")
                .startAfterActivity("set_civil_works_done_status")
                .setVariable("replanStatus", "notreplan")
                .executeWithVariablesInReturn();

        long result = processEngineRule
                .getHistoryService()
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceWithVariables.getProcessInstanceId())
                .activityId("ExclusiveGateway_0it5rnk").count();
        Assert.assertEquals(result, 1L);
    }
}
