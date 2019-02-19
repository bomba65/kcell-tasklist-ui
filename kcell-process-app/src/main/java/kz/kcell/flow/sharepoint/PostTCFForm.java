package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import okhttp3.Protocol;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.variable.value.DateValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.camunda.bpm.engine.delegate.Expression;

import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import java.io.IOException;
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
    private final String username;
    private final String pwd;
    Expression billingTCF;

    @Autowired
    private Environment environment;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    public PostTCFForm(@Value("${sharepoint.forms.url:https://sp.kcell.kz/forms/_api}") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd) {
        this.baseUri = baseUri;
        this.username = username;
        this.pwd = pwd;
    }

    //@Override
    //public void execute(DelegateExecution delegateExecution) throws Exception {

    private static String getAuthenticatedResponse(final String urlStr, final String domain, final String userName, final String password) throws IOException {

        StringBuilder response = new StringBuilder();

        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        URL urlRequest = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "*/*");

        InputStream stream = conn.getInputStream();
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String str = "";
        while ((str = in.readLine()) != null) {
            response.append(str);
        }
        in.close();

        return response.toString();
    }

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
            JSONObject metadataBodyJSON = new JSONObject("{\"type\": \"SP.Data.TCF_x005f_testListItem\"}");
            JSONObject operatorBodyJSON = new JSONObject();
            JSONObject billingTypeBodyJSON = new JSONObject();
            JSONArray operatorResultsJSONArray = new JSONArray();
            JSONArray billingTypeResultsJSONArray = new JSONArray();

            requestBodyJSON.put("__metadata", metadataBodyJSON);

            if("amdocs".equals(billingTCF)){
                headerBillingName = "Amdocs";
                headerBillingId = "Amdocs ID";

                operatorResultsJSONArray.put("Kcell");
                billingTypeResultsJSONArray.put("CBOSS");
                operatorBodyJSON.put("results", operatorResultsJSONArray);
                billingTypeBodyJSON.put("results", billingTypeResultsJSONArray);

                requestBodyJSON.put("Operator", operatorBodyJSON);
                requestBodyJSON.put("BillingType", billingTypeBodyJSON);
            }

            if("orga".equals(billingTCF)){
                headerBillingName = "Orga";
                headerBillingId = "Orga ID";

                operatorResultsJSONArray.put("Activ");
                billingTypeResultsJSONArray.put("Orga");
                operatorBodyJSON.put("results", operatorResultsJSONArray);
                billingTypeBodyJSON.put("results", billingTypeResultsJSONArray);

                requestBodyJSON.put("Operator", operatorBodyJSON);
                requestBodyJSON.put("BillingType", billingTypeBodyJSON);
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
            //requestBodyJSON.put("Status", "Approved by Department Manager");
            requestBodyJSON.put("Created", df.format(new Date()));

            String htmlTemplateTCF = "<div>\n" +
                "    <p></p>\n" +
                "    <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"border: 1px dotted\n" +
                "        #d3d3d3;color:#333333;background-color:#ffffff;width:500px;\">\n" +
                "        <tbody>\n" +
                "            <tr>\n" +
                "                <td rowspan=\"2\" style=\"border: 1px dotted #d3d3d3;height: 54px; width: 120px\">Service Name</td>\n" +
                "                <td rowspan=\"2\" style=\"border: 1px dotted #d3d3d3; width: 60px\">Short Number</td>\n" +
                "                <td colspan=\"3\" style=\"border: 1px dotted #d3d3d3; width: 220px\">" + headerBillingName + "</td>\n" +
                "                <td rowspan=\"2\" style=\"border: 1px dotted #d3d3d3; width: 100px\">Comments for ICTD</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3; width: 66px\">Counter</td>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3; width: 66px\">Price per counter</td>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3;\">" + headerBillingId + "</td>\n" +
                "            </tr>\n" +
                "            <tr>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3;\">" + serviceNameValue + "<br></td>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3;\">" + shortNumberValue + "<br></td>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3;\">" + counterValue + "</td>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3;\">" + pricePerCounterValue + "</td>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3;\"><br></td>\n" +
                "                <td style=\"border: 1px dotted #d3d3d3;\">" + commentValue + "<br></td>\n" +
                "            </tr>\n" +
                "        </tbody>\n" +
                "    </table>\n" +
                "    <p><br></p>\n" +
                "</div>";

            requestBodyJSON.put("Requirments", htmlTemplateTCF);
            delegateExecution.setVariable(billingTCF + "PostRequestBodyTCF", requestBodyJSON.toString());

            if (isSftp) {

                String result = "error";
                try {
                    //String responseText = getAuthenticatedResponse("https://sp.kcell.kz/forms/_api/Lists/getbytitle('TCF_test')/items", "kcell.kz", "camunda_sharepoint", "Bn12#Qaz");
                    String responseText = getAuthenticatedResponse(baseUri + "/Lists/getbytitle('TCF_test')/items", "kcell.kz", username, pwd);
                    result = responseText;
                }catch(Exception e){
                    e.printStackTrace();
                }

                JSONObject responseSharepointJSON = new JSONObject(result);
                if("amdocs".equals(billingTCF)){

                    //delegateExecution.setVariable("amdocsTcfFormStatus", Status);
                    //if("Completed".equals(Status)){
                    if(responseSharepointJSON.has("Id")){
                        delegateExecution.setVariable("amdocsTcfFormId", responseSharepointJSON.get("Id").toString());
                        delegateExecution.setVariable("amdocsTcfFormIdReceived", true);
                    } else {
                        delegateExecution.setVariable("amdocsTcfFormIdReceived", false);
                    }
                }
                if("orga".equals(billingTCF)){

                    //delegateExecution.setVariable("orgaTcfFormStatus", Status);
                    //if("Completed".equals(Status)){
                    if(responseSharepointJSON.has("Id")){
                        delegateExecution.setVariable("orgaTcfFormId", responseSharepointJSON.get("Id").toString());
                        delegateExecution.setVariable("orgaTcfFormIdReceived", true);
                    } else {
                        delegateExecution.setVariable("orgaTcfFormIdReceived", false);
                    }
                }

                /*StringEntity TCFData = new StringEntity(requestBodyJSON.toString(), ContentType.APPLICATION_JSON);

                HttpPost httpPostTCF = new HttpPost(new URI(baseUri+"/Lists/getbytitle('TCF_test')/items"));
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
                //String Status = responseSharepointJSON.get("Status").toString();

                //JSONObject tcf = responseSharepointJSON.getJSONObject("d");
                //String Id = tcf.get("Id").toString();

                if("amdocs".equals(billingTCF)){

                    //delegateExecution.setVariable("amdocsTcfFormStatus", Status);
                    //if("Completed".equals(Status)){
                    if(responseSharepointJSON.has("Id")){
                        delegateExecution.setVariable("amdocsTcfFormId", responseSharepointJSON.get("Id").toString());
                        delegateExecution.setVariable("amdocsTcfFormIdReceived", true);
                    } else {
                        delegateExecution.setVariable("amdocsTcfFormIdReceived", false);
                    }
                }
                if("orga".equals(billingTCF)){

                    //delegateExecution.setVariable("orgaTcfFormStatus", Status);
                    //if("Completed".equals(Status)){
                    if(responseSharepointJSON.has("Id")){
                        delegateExecution.setVariable("orgaTcfFormId", responseSharepointJSON.get("Id").toString());
                        delegateExecution.setVariable("orgaTcfFormIdReceived", true);
                    } else {
                        delegateExecution.setVariable("orgaTcfFormIdReceived", false);
                    }
                }
                */

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
