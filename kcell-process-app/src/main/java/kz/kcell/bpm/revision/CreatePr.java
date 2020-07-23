package kz.kcell.bpm.revision;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

public class CreatePr implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {


        RuntimeService runtimeService = execution.getProcessEngineServices().getRuntimeService();

        Map<String, Object> variables = new HashMap<>();
        if (execution.hasVariable("approve_jr_regionsTaskResult") && execution.getVariable("approve_jr_regionsTaskResult") != null) {
            variables.put("approve_jr_regionsTaskResult", execution.getVariable("approve_jr_regionsTaskResult"));
        }
        if (execution.hasVariable("approve_jrTaskResult") && execution.getVariable("approve_jrTaskResult") != null) {
            variables.put("approve_jrTaskResult", execution.getVariable("approve_jrTaskResult"));
        }
        if (execution.hasVariable("cancelPrComment") && execution.getVariable("cancelPrComment") != null) {
            variables.put("cancelPrComment", execution.getVariable("cancelPrComment"));
        }
        if (execution.hasVariable("centralApproval") && execution.getVariable("centralApproval") != null) {
            variables.put("centralApproval", execution.getVariable("centralApproval"));
        }
        if (execution.hasVariable("centralGroupHeadApprovalComment") && execution.getVariable("centralGroupHeadApprovalComment") != null) {
            variables.put("centralGroupHeadApprovalComment", execution.getVariable("centralGroupHeadApprovalComment"));
        }
        if (execution.hasVariable("centralGroupHeadApprovalTaskResult") && execution.getVariable("centralGroupHeadApprovalTaskResult") != null) {
            variables.put("centralGroupHeadApprovalTaskResult", execution.getVariable("centralGroupHeadApprovalTaskResult"));
        }
        if (execution.hasVariable("contract") && execution.getVariable("contract") != null) {
            variables.put("contract", execution.getVariable("contract"));
        }
        if (execution.hasVariable("contractor") && execution.getVariable("contractor") != null) {
            variables.put("contractor", execution.getVariable("contractor"));
        }
        if (execution.hasVariable("contractorJobAssignedDate") && execution.getVariable("contractorJobAssignedDate") != null) {
            variables.put("contractorJobAssignedDate", execution.getVariable("contractorJobAssignedDate"));
        }
        if (execution.hasVariable("createJRResult") && execution.getVariable("createJRResult") != null) {
            variables.put("createJRResult", execution.getVariable("createJRResult"));
        }
        if (execution.hasVariable("explanation") && execution.getVariable("explanation") != null) {
            variables.put("explanation", execution.getVariable("explanation"));
        }
        if (execution.hasVariable("ignoreSignPR") && execution.getVariable("ignoreSignPR") != null) {
            variables.put("ignoreSignPR", execution.getVariable("ignoreSignPR"));
        }
        if (execution.hasVariable("ignoreSignPRCheckbox") && execution.getVariable("ignoreSignPRCheckbox") != null) {
            variables.put("ignoreSignPRCheckbox", execution.getVariable("ignoreSignPRCheckbox"));
        }
        if (execution.hasVariable("initiatorFull") && execution.getVariable("initiatorFull") != null) {
            variables.put("initiatorFull", execution.getVariable("initiatorFull"));
        }
        if (execution.hasVariable("isNewProcessCreated") && execution.getVariable("isNewProcessCreated") != null) {
            variables.put("isNewProcessCreated", execution.getVariable("isNewProcessCreated"));
        }
        if (execution.hasVariable("isPermitDocsNeeded") && execution.getVariable("isPermitDocsNeeded") != null) {
            variables.put("isPermitDocsNeeded", execution.getVariable("isPermitDocsNeeded"));
        }
        if (execution.hasVariable("jobWorks") && execution.getVariable("jobWorks") != null) {
            variables.put("jobWorks", execution.getVariable("jobWorks"));
        }
        if (execution.hasVariable("jobWorksTotal") && execution.getVariable("jobWorksTotal") != null) {
            variables.put("jobWorksTotal", execution.getVariable("jobWorksTotal"));
        }
        if (execution.hasVariable("jrBlank") && execution.getVariable("jrBlank") != null) {
            variables.put("jrBlank", execution.getVariable("jrBlank"));
        }
        if (execution.hasVariable("jrNumber") && execution.getVariable("jrNumber") != null) {
            variables.put("jrNumber", execution.getVariable("jrNumber"));
        }
        if (execution.hasVariable("leasingCompletionComment") && execution.getVariable("leasingCompletionComment") != null) {
            variables.put("leasingCompletionComment", execution.getVariable("leasingCompletionComment"));
        }
        if (execution.hasVariable("leasingCompletionTaskResult") && execution.getVariable("leasingCompletionTaskResult") != null) {
            variables.put("leasingCompletionTaskResult", execution.getVariable("leasingCompletionTaskResult"));
        }
        if (execution.hasVariable("leasingDone") && execution.getVariable("leasingDone") != null) {
            variables.put("leasingDone", execution.getVariable("leasingDone"));
        }
        if (execution.hasVariable("leasingRequired") && execution.getVariable("leasingRequired") != null) {
            variables.put("leasingRequired", execution.getVariable("leasingRequired"));
        }
        if (execution.hasVariable("leasingRequiredCheckbox") && execution.getVariable("leasingRequiredCheckbox") != null) {
            variables.put("leasingRequiredCheckbox", execution.getVariable("leasingRequiredCheckbox"));
        }
        if (execution.hasVariable("mainContract") && execution.getVariable("mainContract") != null) {
            variables.put("mainContract", execution.getVariable("mainContract"));
        }
        if (execution.hasVariable("materialsRequired") && execution.getVariable("materialsRequired") != null) {
            variables.put("materialsRequired", execution.getVariable("materialsRequired"));
        }
        if (execution.hasVariable("materialsRequiredCheckbox") && execution.getVariable("materialsRequiredCheckbox") != null) {
            variables.put("materialsRequiredCheckbox", execution.getVariable("materialsRequiredCheckbox"));
        }
        if (execution.hasVariable("powerRequired") && execution.getVariable("powerRequired") != null) {
            variables.put("powerRequired", execution.getVariable("powerRequired"));
        }
        if (execution.hasVariable("powerRequiredCheckbox") && execution.getVariable("powerRequiredCheckbox") != null) {
            variables.put("powerRequiredCheckbox", execution.getVariable("powerRequiredCheckbox"));
        }
        if (execution.hasVariable("prCreationInProgress") && execution.getVariable("prCreationInProgress") != null) {
            variables.put("prCreationInProgress", execution.getVariable("prCreationInProgress"));
        }
        if (execution.hasVariable("priority") && execution.getVariable("priority") != null) {
            variables.put("priority", execution.getVariable("priority"));
        }
        if (execution.hasVariable("project") && execution.getVariable("project") != null) {
            variables.put("project", execution.getVariable("project"));
        }
        if (execution.hasVariable("ptype") && execution.getVariable("ptype") != null) {
            variables.put("ptype", execution.getVariable("ptype"));
        }
        if (execution.hasVariable("ptypeCheckbox") && execution.getVariable("ptypeCheckbox") != null) {
            variables.put("ptypeCheckbox", execution.getVariable("ptypeCheckbox"));
        }
        if (execution.hasVariable("reason") && execution.getVariable("reason") != null) {
            variables.put("reason", execution.getVariable("reason"));
        }
        if (execution.hasVariable("regionApproval") && execution.getVariable("regionApproval") != null) {
            variables.put("regionApproval", execution.getVariable("regionApproval"));
        }
        if (execution.hasVariable("regionGroupHeadApprovalComment") && execution.getVariable("regionGroupHeadApprovalComment") != null) {
            variables.put("regionGroupHeadApprovalComment", execution.getVariable("regionGroupHeadApprovalComment"));
        }
        if (execution.hasVariable("regionGroupHeadApprovalTaskResult") && execution.getVariable("regionGroupHeadApprovalTaskResult") != null) {
            variables.put("regionGroupHeadApprovalTaskResult", execution.getVariable("regionGroupHeadApprovalTaskResult"));
        }
        if (execution.hasVariable("relatedTo") && execution.getVariable("relatedTo") != null) {
            variables.put("relatedTo", execution.getVariable("relatedTo"));
        }
        if (execution.hasVariable("requestedDate") && execution.getVariable("requestedDate") != null) {
            variables.put("requestedDate", execution.getVariable("requestedDate"));
        }
        if (execution.hasVariable("resolutions") && execution.getVariable("resolutions") != null) {
            variables.put("resolutions", execution.getVariable("resolutions"));
        }
        if (execution.hasVariable("site") && execution.getVariable("site") != null) {
            variables.put("site", execution.getVariable("site"));
        }
        if (execution.hasVariable("site_name") && execution.getVariable("site_name") != null) {
            variables.put("site_name", execution.getVariable("site_name"));
        }
        if (execution.hasVariable("siteName") && execution.getVariable("siteName") != null) {
            variables.put("siteName", execution.getVariable("siteName"));
        }
        if (execution.hasVariable("siteRegion") && execution.getVariable("siteRegion") != null) {
            variables.put("siteRegion", execution.getVariable("siteRegion"));
        }
        if (execution.hasVariable("siteStatus") && execution.getVariable("siteStatus") != null) {
            variables.put("siteStatus", execution.getVariable("siteStatus"));
        }
        if (execution.hasVariable("soaComplaintId") && execution.getVariable("soaComplaintId") != null) {
            variables.put("soaComplaintId", execution.getVariable("soaComplaintId"));
        }
        if (execution.hasVariable("status") && execution.getVariable("status") != null) {
            variables.put("status", execution.getVariable("status"));
        }
        if (execution.hasVariable("statusHistory") && execution.getVariable("statusHistory") != null) {
            variables.put("statusHistory", execution.getVariable("statusHistory"));
        }
        if (execution.hasVariable("update_leasing_status_specialTaskComment") && execution.getVariable("update_leasing_status_specialTaskComment") != null) {
            variables.put("update_leasing_status_specialTaskComment", execution.getVariable("update_leasing_status_specialTaskComment"));
        }
        if (execution.hasVariable("update_leasing_status_specialTaskCommentVisibility") && execution.getVariable("update_leasing_status_specialTaskCommentVisibility") != null) {
            variables.put("update_leasing_status_specialTaskCommentVisibility", execution.getVariable("update_leasing_status_specialTaskCommentVisibility"));
        }
        if (execution.hasVariable("update_leasing_status_specialTaskResult") && execution.getVariable("update_leasing_status_specialTaskResult") != null) {
            variables.put("update_leasing_status_specialTaskResult", execution.getVariable("update_leasing_status_specialTaskResult"));
        }
        if (execution.hasVariable("validityDate") && execution.getVariable("validityDate") != null) {
            variables.put("validityDate", execution.getVariable("validityDate"));
        }
        if (execution.hasVariable("workPrices") && execution.getVariable("workPrices") != null) {
            variables.put("workPrices", execution.getVariable("workPrices"));
        }
        if (execution.hasVariable("worksBelongsTo") && execution.getVariable("worksBelongsTo") != null) {
            variables.put("worksBelongsTo", execution.getVariable("worksBelongsTo"));
        }
        if (execution.hasVariable("worksPriceList") && execution.getVariable("worksPriceList") != null) {
            variables.put("worksPriceList", execution.getVariable("worksPriceList"));
        }
        if (execution.hasVariable("workTitlesForSearch") && execution.getVariable("workTitlesForSearch") != null) {
            variables.put("workTitlesForSearch", execution.getVariable("workTitlesForSearch"));
        }
//        if (execution.hasVariable("starter") && execution.getVariable("starter") != null) {
//            variables.put("starter", execution.getVariable("starter"));
//        }
        ProcessInstance message_start_pr_2 = runtimeService.startProcessInstanceByMessage("Message_start_pr_2", "PR-" + execution.getProcessBusinessKey(), variables);

        if (message_start_pr_2 != null) {
            System.out.println(message_start_pr_2.getId());
        } else {
            System.out.println("NO NULLLLLLL");
        }
    }
}
