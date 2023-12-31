package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.commons.lang.RandomStringUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


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
        String provider = String.valueOf(delegateExecution.getVariable("provider"));
        String clientCompanyLatName = String.valueOf(delegateExecution.getVariable("clientCompanyLatName"));
        String officialClientCompanyName = String.valueOf(delegateExecution.getVariable("officialClientCompanyName"));
        String smsGwUsername = clientCompanyLatName + "_smsgw3_user";
        String smsGwUserId = null;
        String smsGwSenderId = null;
        String smsGwBwListId = null;
        JSONArray identifierJSONArray = new JSONArray(String.valueOf(delegateExecution.getVariable("identifiers")));
        JSONObject identifierJSON = identifierJSONArray.getJSONObject(0);
        JSONArray operators = identifierJSON.getJSONArray("operators");
        String identifier = String.valueOf(identifierJSON.get("title"));

        if ("rest".equals(connectionType) && ("alfanumeric".equals(identifierType) || "digital".equals(identifierType))) {
            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            String responseGetUsers = executeGet(baseUri + "/users/", closeableHttpClient);
            String smsGwPasswd = generateCommonLangPassword();

            log.info("responseGetUsersRequest :  " + responseGetUsers);

            JSONArray responseGetUsersJson = new JSONArray(responseGetUsers);
            delegateExecution.setVariable("responseGetUsers", responseGetUsers);

            String clientBIN = String.valueOf(delegateExecution.getVariable("clientBIN"));

            log.info("clientBIN " + clientBIN);

            boolean userAlreadyExists = false;

            for (int i = 0; i < responseGetUsersJson.length(); i++) {
                if (((JSONObject) responseGetUsersJson.get(i)).get("username").equals(smsGwUsername)) {
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
                jsonObject.put("comments", officialClientCompanyName);
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

                String channelName = "SMS";
                JSONObject jsonPutChannel = new JSONObject();
                jsonPutChannel.put("channelName", channelName);
                jsonPutChannel.put("userId", Integer.parseInt(smsGwUserId));
                log.info("jsonputchannel" + jsonPutChannel);
                String responsePutChannel = executePut(baseUri + "/users/channels/", closeableHttpClient, jsonPutChannel);
                delegateExecution.setVariable("responsePutChannel", responsePutChannel);
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
                String moUrl = delegateExecution.getVariable("moUrl") != null ? String.valueOf(delegateExecution.getVariable("moUrl")) : null;
                String moLogin = delegateExecution.getVariable("moLogin") != null ? String.valueOf(delegateExecution.getVariable("moLogin")) : null;
                String moPassword = delegateExecution.getVariable("moPassword") != null ? String.valueOf(delegateExecution.getVariable("moPassword")) : null;
                String deliveryReport = delegateExecution.getVariable("deliveryReport") != null ? String.valueOf(delegateExecution.getVariable("deliveryReport")) : null;
                Boolean drBool = deliveryReport != null && deliveryReport.equals("true");
                String receivingServerAddress = delegateExecution.getVariable("receivingServerAddress") != null ? String.valueOf(delegateExecution.getVariable("receivingServerAddress")) : null;
                String receivingServerLogin = delegateExecution.getVariable("receivingServerLogin") != null ? String.valueOf(delegateExecution.getVariable("receivingServerLogin")) : null;
                String receivingServerPass = delegateExecution.getVariable("receivingServerPass") != null ? String.valueOf(delegateExecution.getVariable("receivingServerPass")) : null;

                String queueName = identifier.replaceAll(" ", "_") + "_" + (smsServiceType.equals("MO") ? "mo_" : "") + "queue";
                Integer accountConfigId = null;

                if (operatorType.equals("onnet") && smsServiceType.equals("MT")) {
                    accountConfigId = -46;
                } else if (operatorType.equals("onnet") && smsServiceType.equals("MO")) {
                    accountConfigId = 4;
                } else if (operatorType.equals("offnet")) {

                    if (provider.equals("SMS Consult") && smsServiceType.equals("MT")) {
                        accountConfigId = 2;
                    }
                    if (provider.equals("MMS") && smsServiceType.equals("MT")) {
                        accountConfigId = -42;
                    }

                    if (provider.equals("SMS Consult") && smsServiceType.equals("MO")) {
                        accountConfigId = -41;
                    }

                    if (provider.equals("MMS") && smsServiceType.equals("MO")) {
                        accountConfigId = 5;
                    }

                    if (provider.equals("KIT") && smsServiceType.equals("MT")) {
                        accountConfigId = 7; }

                    if (provider.equals("KIT") && smsServiceType.equals("MO")) {
                        accountConfigId = 6; }
                }

                if (accountConfigId == null) {
                    return;
                }

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("accountConfigId", accountConfigId);
                jsonObject.put("billingId", "");
                jsonObject.put("drBatchSize", 100);
                jsonObject.put("drLink", drBool ? receivingServerAddress : "http://192.168.217.46:8666/delivery");
                jsonObject.put("drPassword", receivingServerPass != null ? receivingServerPass : "");
                jsonObject.put("drPeriod", 30);
                jsonObject.put("drUsername", receivingServerLogin != null ? receivingServerLogin : "");
                jsonObject.put("isActive", true);
                jsonObject.put("isSendDr", true);
                jsonObject.put("isSendMo", smsServiceType.equals("MO"));
                jsonObject.put("moBatchSize", 500);
                jsonObject.put("moLink", moUrl != null ? moUrl : "");
                jsonObject.put("moPassword", moPassword != null ? moPassword : "");
                jsonObject.put("moPeriod", 30);
                jsonObject.put("moUsername", moLogin != null ? moLogin : "");
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
                for (int i = 0; i < operators.length(); i++) {
                    JSONObject jsonMapping = new JSONObject();
                    if (((JSONObject) operators.get(i)).has("isAvailable") && ((JSONObject) operators.get(i)).getBoolean("isAvailable")) {
                        jsonMapping.put("mappingId", 0);
                        jsonMapping.put("operator", ((JSONObject) operators.get(i)).get("id").toString().toUpperCase());
                        jsonMapping.put("originalSender", identifier);
                        jsonMapping.put("outputSender", "digital".equals(identifierType) ? identifier : ((JSONObject) operators.get(i)).get("title").toString());
                        String responsePut = executePut(baseUri + "/sender-mappings/", closeableHttpClient, jsonMapping);
                        delegateExecution.setVariable("putMapping", responsePut);
                    }
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
                String[] testNumbers = String.valueOf(delegateExecution.getVariable("testNumber")).split(",");
                StringBuilder regex = new StringBuilder("^7(");

                for (String number: testNumbers) {
                    number = number.substring(1);
                    regex.append(number).append("|");
                }

                regex.append(")");

                log.info(" regex " +  regex);

                String testNumber = String.valueOf(delegateExecution.getVariable("testNumber"));
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("sender_id", Integer.parseInt(smsGwSenderId));
                jsonObject.put("recipient_regex", regex.toString());
                jsonObject.put("type_of_bw", "W");
                jsonObject.put("comments", "test");

                log.info("jsonputbwlist  " + jsonObject);
                String responsePut = executePut(baseUri + "/black_white_lists/", closeableHttpClient, jsonObject);
                JSONObject responsePutBwListJson = new JSONObject(responsePut);
                smsGwBwListId = responsePutBwListJson.getString("bw_list_id");

                log.info("responsePut " + responsePut);
                delegateExecution.setVariable("smsGwBwListId", smsGwBwListId);
            }
        } else if (connectionType.equals("smpp")){
            String userName = clientCompanyLatName + "_smpp";
            String password = generateCommonLangPassword();
            delegateExecution.setVariable("clientLogin", userName);
            delegateExecution.setVariable("clientPassword", password);
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

    private String generateCommonLangPassword() {
        String upperCaseLetters = RandomStringUtils.random(2, 65, 90, true, true);
        String lowerCaseLetters = RandomStringUtils.random(2, 97, 122, true, true);
        String numbers = RandomStringUtils.randomNumeric(2);
        String totalChars = RandomStringUtils.randomAlphanumeric(2);
        String combinedChars = upperCaseLetters.concat(lowerCaseLetters)
            .concat(numbers)
            .concat(totalChars);
        List<Character> pwdChars = combinedChars.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());
        Collections.shuffle(pwdChars);
        String password = pwdChars.stream()
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();
        return password;
    }


}
