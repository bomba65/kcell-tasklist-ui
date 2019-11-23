package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Base64;


@Service("createSMSGWClient")
@Log
public class CreateSMSGWClient implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String baseUri;
    private final String authStr64;


    @Autowired
    public CreateSMSGWClient(@Value("https://admin-api-hermes.kcell.kz") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd) {
        this.baseUri = baseUri;
        this.authStr64 = username + ":" + pwd;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String connectionType = String.valueOf(delegateExecution.getVariable("connectionType"));
        String identifierType = String.valueOf(delegateExecution.getVariable("identifierType"));
        String operatorType = String.valueOf(delegateExecution.getVariable("operatorType"));
        String provider = String.valueOf(delegateExecution.getVariable("provider "));
        String clientCompanyLatName = String.valueOf(delegateExecution.getVariable("clientCompanyLatName"));
        String smsGwUsername = clientCompanyLatName + "_REST";
        String smsGwUserId = null;
        String smsGwSenderId = null;
        String smsGwBwListId = null;
        JSONArray identifierJSONArray = new JSONArray(String.valueOf(delegateExecution.getVariable("identifiers")));
        JSONObject identifierJSON = identifierJSONArray.getJSONObject(0);
        JSONArray operators  =identifierJSON.getJSONArray("operators");
        String identifier = String.valueOf(identifierJSON.get("title"));

        if ("rest".equals(connectionType) && ("alfanumeric".equals(identifierType) || "digital".equals(identifierType)) ) {
            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            String responseGetUsers = executeGet(baseUri + "/users/", closeableHttpClient);
            String smsGwPasswd = "123456789";

            log.info("responseGetUsersRequest :  " + responseGetUsers);

            JSONArray responseGetUsersJson = new JSONArray(responseGetUsers);
            delegateExecution.setVariable("responseGetUsers", responseGetUsers);

            String clientBIN = String.valueOf(delegateExecution.getVariable("clientBIN"));

            log.info("clientBIN " + clientBIN);

            boolean userAlreadyExists = false;

            for (int i = 0; i < responseGetUsersJson.length(); i++) {
                if (((JSONObject) responseGetUsersJson.get(i)).get("username").equals(clientCompanyLatName)) {
                    userAlreadyExists = true;
                    smsGwUserId = ((JSONObject) responseGetUsersJson.get(i)).getString("userId");
                    break;
                }
            }
            log.info("userAlreadyExists " + userAlreadyExists);
            if (!userAlreadyExists) {
                String comments = String.valueOf(delegateExecution.getVariable("comments"));

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("bin", clientBIN);
                jsonObject.put("comments", comments);
                jsonObject.put("isActive", true);
                jsonObject.put("isReadOnly", false);
                jsonObject.put("password", smsGwPasswd);
                jsonObject.put("userId", 0);
                jsonObject.put("username", smsGwUsername);

                log.info("userPUTjson  " + jsonObject);
                String responsePut = executePut(baseUri + "/users/", closeableHttpClient, jsonObject);
                JSONObject responsePutUserJson = new JSONObject(responsePut);
                smsGwUserId = responsePutUserJson.getString("userId");
                log.info("responsePut " + responsePut);
                delegateExecution.setVariable("smsGwUserId", smsGwUserId);
                delegateExecution.setVariable("putUserResponse", responsePut);

                if (operatorType.equals("onnet")) {
                    String channelName = "SMS";
                    JSONObject jsonPutChannel = new JSONObject();
                    jsonPutChannel.put("channelName", channelName);
                    jsonPutChannel.put("userId", Integer.parseInt(smsGwUserId));
                    log.info("jsonputchannel" + jsonPutChannel);
                    String responsePutChannel = executePut(baseUri + "/users/channels/", closeableHttpClient, jsonPutChannel);
                    delegateExecution.setVariable("responsePutChannel", responsePutChannel);
                }
            }

            String responseGetSenders = executeGet(baseUri + "/senders/", closeableHttpClient);

            JSONArray responseGetSendersJson = new JSONArray(responseGetSenders);
            delegateExecution.setVariable("responseGetSenders", responseGetSenders);
            delegateExecution.setVariable("clientLogin", smsGwUsername);
            delegateExecution.setVariable("clientPassword", smsGwPasswd);
            delegateExecution.setVariable("sendPreferencesToClientTaskResult", "approve");

            log.info("responseGetSenders " + responseGetSenders);

            boolean senderAlreadyExists = false;

            for (int i = 0; i < responseGetSendersJson.length(); i++) {
                if (((JSONObject) responseGetSendersJson.get(i)).get("senderName").equals(identifier)) {
                    senderAlreadyExists = true;
                    smsGwSenderId = ((JSONObject) responseGetSendersJson.get(i)).getString("senderId");
                    delegateExecution.setVariable("smsGwSenderId", smsGwSenderId);
                    break;
                }
            }
            log.info("senderAlreadyExists " + senderAlreadyExists);
            log.info("smsGwUserId " + smsGwUserId);
            if (!senderAlreadyExists) {
                String smsServiceType = String.valueOf(delegateExecution.getVariable("smsServiceType"));
                String queueName = identifier + "_" + (smsServiceType.equals("MO") ? "mo_" : "") + "queue";
                Integer accountConfigId = null;

                if (operatorType.equals("onnet") && smsServiceType.equals("MT")) {
                    accountConfigId = -46;
                } else {
                    switch (provider) {
                        case "SMS Consult":
                            accountConfigId = 2;
                            break;
                        case "MMS":
                            accountConfigId = -42;
                            break;
                    }
                }

                if (accountConfigId == null) {
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("accountConfigId", accountConfigId);
                jsonObject.put("billingId", "");
                jsonObject.put("drBatchSize", 0);
                jsonObject.put("drLink", "");
                jsonObject.put("drPassword", "");
                jsonObject.put("drPeriod", 0);
                jsonObject.put("drUsername", "");
                jsonObject.put("isActive", true);
                jsonObject.put("isSendDr", false);
                jsonObject.put("isSendMo", smsServiceType.equals("MO"));
                jsonObject.put("moBatchSize", 0);
                jsonObject.put("moLink", "");
                jsonObject.put("moPassword", "");
                jsonObject.put("moPeriod", 0);
                jsonObject.put("moUsername", "");
                jsonObject.put("queueName", queueName);
                jsonObject.put("senderName", identifier);
                jsonObject.put("throttlingOffnet", 100);
                jsonObject.put("throttlingOnnet", 2000);
                jsonObject.put("userId", Integer.parseInt(smsGwUserId));

                log.info("jsonputsender " + jsonObject);

                String responsePut = executePut(baseUri + "/senders/", closeableHttpClient, jsonObject);
                JSONObject responsePutSenderJson = new JSONObject(responsePut);
                smsGwSenderId = responsePutSenderJson.getString("senderId");

                delegateExecution.setVariable("smsGwSenderId", smsGwSenderId);
                delegateExecution.setVariable("putSenderResponse", responsePut);
            }

            if (operatorType.equals("offnet")) {
                for (int i = 0; i < operators.length(); i++){
                    JSONObject jsonMapping = new JSONObject();
                    jsonMapping.put("mappingId", 0);
                    jsonMapping.put("operator", ((JSONObject) operators.get(i)).get("id").toString().toUpperCase());
                    jsonMapping.put("originalSender", identifier);
                    jsonMapping.put("outputSender", ((JSONObject) operators.get(i)).get("title").toString());
                    String responsePut = executePut(baseUri + "/mappings/", closeableHttpClient, jsonMapping);
                    delegateExecution.setVariable("putMapping", responsePut);
                }
            }

            String responseGetBWLists = executeGet(baseUri + "/black_white_lists/", closeableHttpClient);

            JSONArray responseGetBWListsJson = new JSONArray(responseGetBWLists);
            delegateExecution.setVariable("responseGetBWLists", responseGetBWLists);
            log.info("responseGetBWLists " + responseGetBWLists);

            boolean bwListAlreadyExists = false;

            for (int i = 0; i < responseGetBWListsJson.length(); i++) {
                if (((JSONObject) responseGetBWListsJson.get(i)).get("sender_id").toString().equals(smsGwSenderId)) {
                    bwListAlreadyExists = true;
                    smsGwBwListId = ((JSONObject) responseGetBWListsJson.get(i)).getString("bw_list_id");
                    delegateExecution.setVariable("smsGwBwListId", smsGwBwListId);
                    break;
                }
            }

            log.info("bwListAlreadyExists " + bwListAlreadyExists);
            log.info("smsGwSenderId " + smsGwSenderId);

            if (!bwListAlreadyExists) {
                String testNumber = String.valueOf(delegateExecution.getVariable("testNumber"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("sender_id", Integer.parseInt(smsGwSenderId));
                jsonObject.put("recipient_regex", testNumber);
                jsonObject.put("type_of_bw", "W");
                jsonObject.put("comments", "test");

                log.info("jsonputbwlist  " + jsonObject);
                String responsePut = executePut(baseUri + "/black_white_lists/", closeableHttpClient, jsonObject);
                JSONObject responsePutBwListJson = new JSONObject(responsePut);
                smsGwBwListId = responsePutBwListJson.getString("bw_list_id");

                log.info("responsePut " + responsePut);
                delegateExecution.setVariable("smsGwBwListId", smsGwBwListId);
            }
        }
    }

    private String executeGet(String url, HttpClient httpClient) throws Exception {
        String encoding = Base64.getEncoder().encodeToString((authStr64).getBytes("UTF-8"));
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Authorization", "Basic " + encoding);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity productCatalogEntity = httpResponse.getEntity();
        log.info("GET uri: " + url + " statusCode " + httpResponse.getStatusLine().getStatusCode());
        return EntityUtils.toString(productCatalogEntity);
    }

    private String executePut(String url, HttpClient httpClient, JSONObject requestBody) throws Exception {
        String encoding = Base64.getEncoder().encodeToString((authStr64).getBytes("UTF-8"));
        StringEntity entity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);

        HttpPut httpPut = new HttpPut(url);
        httpPut.setHeader("Authorization", "Basic " + encoding);
        httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPut.setEntity(entity);
        HttpResponse httpResponse = httpClient.execute(httpPut);
        HttpEntity responseEntity = httpResponse.getEntity();
        String strEntity = EntityUtils.toString(responseEntity);
        log.info("method " + httpPut.getMethod());
        log.info("PUT uri: " + url + " statusCode " + httpResponse.getStatusLine().getStatusCode());
        log.info("PUT uri: " + url + " strEntity " + strEntity);
        return strEntity;
    }


}
