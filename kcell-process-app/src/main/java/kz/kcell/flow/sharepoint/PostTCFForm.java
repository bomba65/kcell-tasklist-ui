package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.delegate.Expression;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;


//@Service("postTCFForm")
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Log
public class PostTCFForm implements ExecutionListener {
    private final String baseUri;
    Expression billingTCF;

    @Autowired
    private Environment environment;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    public PostTCFForm(@Value("${sharepoint.forms.url:https://sp.kcell.kz/forms/_api/Lists}") String baseUri) {
        this.baseUri = baseUri;
    }

    //@Override
    //public void execute(DelegateExecution delegateExecution) throws Exception {

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

        /*
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }
        */

        String processKey = repositoryService.createProcessDefinitionQuery().processDefinitionId(delegateExecution.getProcessDefinitionId()).list().get(0).getKey();
        String billingTCF = this.billingTCF.getValue(delegateExecution).toString();

        String taskResolutionResult = "";
        String ServiceNameRUS = "";
        String ServiceNameENG = "";
        String ServiceNameKAZ = "";

        String tcfDateValue = "";
        String commentValue = "";

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        System.out.println("processKey: " + processKey);

        if ("bulksmsConnectionKAE".equals(processKey)) {

            ServiceNameRUS = "Bulk sms";
            ServiceNameENG = "Bulk sms";
            ServiceNameKAZ = "Bulk sms";

            if("amdocs".equals(billingTCF)){
                taskResolutionResult = delegateExecution.getVariable("massApprove_bulkSMS_checkFormAmdocsTCFTaskResult").toString();
                commentValue = delegateExecution.getVariable("massApprove_bulkSMS_checkFormAmdocsTCFTaskComment").toString();

                Date closeDate = delegateExecution.<DateValue>getVariableTyped("massApprove_bulkSMS_confirmAmdocsTCFTaskCloseDate").getValue();
                tcfDateValue = df.format(closeDate);
                //tcfDateValue = String.valueOf(delegateExecution.getVariable("massApprove_bulkSMS_confirmAmdocsTCFTaskCloseDate"));
                //tcfDateValue = String.valueOf(delegateExecution.getVariable("massApprove_bulkSMS_checkFormOrgaTCFTaskCloseDate"));
            }
            if("orga".equals(billingTCF)){
                taskResolutionResult = delegateExecution.getVariable("massApprove_bulkSMS_checkFormOrgaTCFTaskResult").toString();
                commentValue = delegateExecution.getVariable("massApprove_bulkSMS_checkFormOrgaTCFTaskComment").toString();

                Date closeDate = delegateExecution.<DateValue>getVariableTyped("massApprove_bulkSMS_confirmOrgaTCFTaskCloseDate").getValue();
                tcfDateValue = df.format(closeDate);
                //tcfDateValue = String.valueOf(delegateExecution.getVariable("massApprove_bulkSMS_confirmOrgaTCFTaskCloseDate"));
                //tcfDateValue = String.valueOf(delegateExecution.getVariable("massApprove_bulkSMS_checkFormOrgaTCFTaskCloseDate"));
            }

        }
        if ("freephone".equals(processKey)) {

            ServiceNameRUS = "Free Phone";
            ServiceNameENG = "Free Phone";
            ServiceNameKAZ = "Free Phone";

            if("amdocs".equals(billingTCF)){
                taskResolutionResult = delegateExecution.getVariable("massApprove_checkFormAmdocsTCFTaskResult").toString();
                commentValue = delegateExecution.getVariable("massApprove_checkFormAmdocsTCFTaskComment").toString();

                Date closeDate = delegateExecution.<DateValue>getVariableTyped("massApprove_confirmAmdocsTCFTaskCloseDate").getValue();
                tcfDateValue = df.format(closeDate);
                //tcfDateValue = String.valueOf(delegateExecution.getVariable("massApprove_confirmAmdocsTCFTaskCloseDate"));
                //tcfDateValue = String.valueOf(delegateExecution.getVariable("massApprove_checkFormAmdocsTCFTaskCloseDate"));
            }
            if("orga".equals(billingTCF)){
                taskResolutionResult = delegateExecution.getVariable("massApprove_checkFormOrgaTCFTaskResult").toString();
                commentValue = delegateExecution.getVariable("massApprove_checkFormOrgaTCFTaskComment").toString();

                Date closeDate = delegateExecution.<DateValue>getVariableTyped("massApprove_confirmOrgaTCFTaskCloseDate").getValue();
                tcfDateValue = df.format(closeDate);
                //tcfDateValue = String.valueOf(delegateExecution.getVariable("massApprove_confirmOrgaTCFTaskCloseDate"));
                //tcfDateValue = String.valueOf(delegateExecution.getVariable("massApprove_checkFormOrgaTCFTaskCloseDate"));
            }

        }

        System.out.println("taskResolutionResult: " + taskResolutionResult + ", taskCloseDate: " + tcfDateValue);

        if (!"rejected".equals(taskResolutionResult)) {

            Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

            JSONObject identifierJSON = new JSONObject(delegateExecution.getVariable("identifier").toString());
            String shortNumberValue = identifierJSON.get("title").toString();

            String serviceNameValue = String.valueOf(delegateExecution.getVariable("identifierServiceName"));
            String counterValue = String.valueOf(delegateExecution.getVariable("identifierCounter"));
            String pricePerCounterValue = String.valueOf(delegateExecution.getVariable("abonentTarif"));

            String headerBillingName = "";
            String headerBillingId = "";

            JSONObject requestBodyJSON = new JSONObject();

            if("amdocs".equals(billingTCF)){
                headerBillingName = "Amdocs";
                headerBillingId = "Amdocs ID";
                requestBodyJSON.put("Operator", "Kcell");
                requestBodyJSON.put("BillingType", "CBOSS");
            }

            if("orga".equals(billingTCF)){
                headerBillingName = "Orga";
                headerBillingId = "Orga ID";
                requestBodyJSON.put("Operator", "Activ");
                requestBodyJSON.put("BillingType", "Orga");

            }

            requestBodyJSON.put("InitiatorDepartment", "B2B");
            requestBodyJSON.put("Subject", "B2B short numbers");
            requestBodyJSON.put("DateDeadline", tcfDateValue);

            requestBodyJSON.put("Service", "Products / Tariffs");
            requestBodyJSON.put("RelationWithThirdParty", false);
            requestBodyJSON.put("TypeForm", "Изменение тарифа на существующий сервис (New service TCF)");
            requestBodyJSON.put("Comments", commentValue);
            requestBodyJSON.put("ServiceNameRUS", ServiceNameRUS);
            requestBodyJSON.put("ServiceNameENG", ServiceNameENG);
            requestBodyJSON.put("ServiceNameKAZ", ServiceNameKAZ);
            requestBodyJSON.put("Status", "Approved by Department Manager");

            String htmlTemplateTCF = "<table>\n" +
                "<tbody>\n" +
                " <tr>\n" +
                "  <td rowspan=2> Service Name </td>\n" +
                "  <td rowspan=2>Short Number</td>\n" +
                "  <td colspan=3>" + headerBillingName + "</td>\n" +
                "  <td rowspan=2>Дата поставки</td> \n" +
                "  <td rowspan=2>Comments for ICTD</td> \n" +
                " </tr>\n" +
                " <tr>\n" +
                "  <td>Counter</td>\n" +
                "  <td>Price per counter</td>\n" +
                "  <td>" + headerBillingId + "</td>\n" +
                " </tr>\n" +
                " <tr> \n" +
                "  <td>" + serviceNameValue + "</td>\n" +
                "  <td>" + shortNumberValue + "</td>\n" +
                "  <td>" + counterValue + "</td>\n" +
                "  <td>" + pricePerCounterValue + "</td>\n" +
                "  <td> </td>\n" +
                "  <td>" + tcfDateValue + "</td>\n" +
                "  <td>" + commentValue + "</td>\n" +
                " </tr>\n" +
                "</tbody>\n" +
                "</table>";

            requestBodyJSON.put("Requirments", htmlTemplateTCF);
            delegateExecution.setVariable(billingTCF + "PostRequestBodyTCF", requestBodyJSON.toString());

            if (isSftp) {

                StringEntity TCFData = new StringEntity(requestBodyJSON.toString(), ContentType.APPLICATION_JSON);

                HttpPost httpPostTCF = new HttpPost(new URI(baseUri+"/getbytitle('ICTD%20TCF')"));
                httpPostTCF.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPostTCF.setEntity(TCFData);

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());

                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

                HttpResponse response = httpClient.execute(httpPostTCF);
                HttpEntity entity = response.getEntity();
                String responseString = EntityUtils.toString(entity, "UTF-8");

                delegateExecution.setVariable(billingTCF + "PostResponseBodyTCF", responseString);

                // What is the format of returned response from POST method?
                JSONObject responseSharepointJSON = new JSONObject(responseString);
                //String Id = responseSharepointJSON.get("Id").toString();
                String Status = responseSharepointJSON.get("Status").toString();

                //JSONObject tcf = responseSharepointJSON.getJSONObject("d");
                //String Id = tcf.get("Id").toString();

                if("amdocs".equals(billingTCF)){

                    delegateExecution.setVariable("amdocsTcfFormStatus", Status);
                    if("Completed".equals(Status)){
                        delegateExecution.setVariable("amdocsTcfFormId", responseSharepointJSON.get("Id").toString());
                        delegateExecution.setVariable("amdocsTcfFormIdReceived", true);
                    } else {
                        delegateExecution.setVariable("amdocsTcfFormIdReceived", false);
                    }
                }
                if("orga".equals(billingTCF)){

                    delegateExecution.setVariable("orgaTcfFormStatus", Status);
                    if("Completed".equals(Status)){
                        delegateExecution.setVariable("orgaTcfFormId", responseSharepointJSON.get("Id").toString());
                        delegateExecution.setVariable("orgaTcfFormIdReceived", true);
                    } else {
                        delegateExecution.setVariable("orgaTcfFormIdReceived", false);
                    }
                }
            } else {
                if("amdocs".equals(billingTCF)) {
                    delegateExecution.setVariable("amdocsTcfFormId", 1111);
                    delegateExecution.setVariable("amdocsTcfFormIdReceived", true);
                }
                if("orga".equals(billingTCF)) {
                    delegateExecution.setVariable("orgaTcfFormId", 2222);
                    delegateExecution.setVariable("orgaTcfFormIdReceived", true);
                }
            }
        } else {
            if("amdocs".equals(billingTCF)) {
                delegateExecution.setVariable("amdocsTcfFormIdReceived", false);
            }
            if("orga".equals(billingTCF)) {
                delegateExecution.setVariable("orgaTcfFormIdReceived", false);
            }
        }
    }
}
