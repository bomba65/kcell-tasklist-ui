package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.delegate.Expression;

import java.net.URI;
import java.util.Arrays;


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

        System.out.println("processKey: " + processKey);



        if ("bulksmsConnectionKAE".equals(processKey)) {

            if("amdocs".equals(billingTCF)){
                taskResolutionResult = delegateExecution.getVariable("massApprove_bulkSMS_checkFormAmdocsTCFTaskResult").toString();
            }
            if("orga".equals(billingTCF)){
                taskResolutionResult = delegateExecution.getVariable("massApprove_bulkSMS_checkFormOrgaTCFTaskResult").toString();
            }

        }
        if ("freephone".equals(processKey)) {

            if("amdocs".equals(billingTCF)){
                taskResolutionResult = delegateExecution.getVariable("massApprove_checkFormAmdocsTCFTaskResult").toString();
            }
            if("orga".equals(billingTCF)){
                taskResolutionResult = delegateExecution.getVariable("massApprove_checkFormOrgaTCFTaskResult").toString();
            }

        }

        System.out.println("taskResolutionResult: " + taskResolutionResult);

        //String bulkSMS_checkFormAmdocsTCFTaskResult = delegateExecution.getVariable("massApprove_bulkSMS_checkFormAmdocsTCFTaskResult").toString();
        //String freephone_checkFormAmdocsTCFTaskResult = delegateExecution.getVariable("massApprove_checkFormAmdocsTCFTaskResult").toString();

        //System.out.println("bulkSMS: " + bulkSMS_checkFormAmdocsTCFTaskResult + ", freepphone: " + freephone_checkFormAmdocsTCFTaskResult);

        if (!"rejected".equals(taskResolutionResult)) {

            Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

            //boolean amdocsBilling = ((Boolean) delegateExecution.getVariable("amdocsBilling")).booleanValue();
            //boolean orgaBilling = ((Boolean) delegateExecution.getVariable("orgaBilling")).booleanValue();

            //String billingTCF = this.billingTCF.getValue(delegateExecution).toString();



            if (isSftp) {
                JSONObject requestBodyJSON = new JSONObject();

                if("amdocs".equals(billingTCF)){
                    requestBodyJSON.put("InitiatorDepartment", "B2B");
                    requestBodyJSON.put("Subject", "B2B short numbers");
                    requestBodyJSON.put("DateDeadline", String.valueOf(delegateExecution.getVariable("massApprove_confirmAmdocsTCFTaskCloseDate"))); //yyyy-MM-ddTHH:mm:ss
                    requestBodyJSON.put("Operator", "Kcell");
                    requestBodyJSON.put("BillingType", "CBOSS");
                    requestBodyJSON.put("Service", "Products / Tariffs");
                    requestBodyJSON.put("RelationWithThirdParty", false);
                    requestBodyJSON.put("TypeForm", "Изменение тарифа на существующий сервис (New service TCF)");
                    requestBodyJSON.put("ServiceNameRUS", "Free Phone");
                    requestBodyJSON.put("ServiceNameENG", "Free Phone");
                    requestBodyJSON.put("ServiceNameKAZ", "Free Phone");
                    requestBodyJSON.put("Requirments", "");
                    requestBodyJSON.put("Comments", String.valueOf(delegateExecution.getVariable("massApprove_checkFormAmdocsTCFTaskComment")));
                }

                if("orga".equals(billingTCF)){
                    requestBodyJSON.put("InitiatorDepartment", "B2B");
                    requestBodyJSON.put("Subject", "B2B short numbers");
                    requestBodyJSON.put("DateDeadline", String.valueOf(delegateExecution.getVariable("massApprove_confirmOrgaTCFTaskCloseDate"))); //yyyy-MM-ddTHH:mm:ss
                    requestBodyJSON.put("Operator", "Activ");
                    requestBodyJSON.put("BillingType", "Orga");
                    requestBodyJSON.put("Service", "Products / Tariffs");
                    requestBodyJSON.put("RelationWithThirdParty", false);
                    requestBodyJSON.put("TypeForm", "Изменение тарифа на существующий сервис (New service TCF)");
                    requestBodyJSON.put("ServiceNameRUS", "Bulk sms");
                    requestBodyJSON.put("ServiceNameENG", "Bulk sms");
                    requestBodyJSON.put("ServiceNameKAZ", "Bulk sms");
                    requestBodyJSON.put("Requirments", "");
                    requestBodyJSON.put("Comments", String.valueOf(delegateExecution.getVariable("massApprove_checkFormOrgaTCFTaskComment")));
                }
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

                // What is the format of returned response from POST method?
                JSONObject responseSharepointJSON = new JSONObject(responseString);
                String Id = responseSharepointJSON.get("Id").toString();

                //JSONObject tcf = responseSharepointJSON.getJSONObject("d");
                //String Id = tcf.get("Id").toString();

                if("amdocs".equals(billingTCF)){
                    delegateExecution.setVariable("amdocsTcfFormId", Id);
                    delegateExecution.setVariable("amdocsTcfFormIdReceived", true);
                }
                if("orga".equals(billingTCF)){
                    delegateExecution.setVariable("orgaTcfFormId", Id);
                    delegateExecution.setVariable("orgaTcfFormIdReceived", true);
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
            delegateExecution.setVariable("amdocsTcfFormIdReceived", false);
            delegateExecution.setVariable("orgaTcfFormIdReceived", false);
        }
    }
}
