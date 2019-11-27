package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;


@Service("UpdateBWList")
@Log
public class UpdateBWList implements JavaDelegate {
    private final String baseUri;
    private final String authStr64;

    @Autowired
    public UpdateBWList(@Value("https://admin-api-hermes.kcell.kz") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd) {
        this.baseUri = baseUri;
        this.authStr64 = username + ":" + pwd;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String connectionType = String.valueOf(delegateExecution.getVariable("connectionType"));
        String smsGwSenderId = String.valueOf(delegateExecution.getVariable("smsGwSenderId"));
        String smsGwBwListId = String.valueOf(delegateExecution.getVariable("smsGwBwListId"));
        String identifierType = String.valueOf(delegateExecution.getVariable("identifierType"));

        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();

        if ("rest".equals(connectionType) && "alfanumeric".equals(identifierType)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("bw_list_id", smsGwBwListId);
            jsonObject.put("sender_id", smsGwSenderId);
            jsonObject.put("recipient_regex", "^7\\d{10}");
            jsonObject.put("type_of_bw", "W");
            jsonObject.put("comments", "test");

            String responsePost = executePost(baseUri + "/black_white_lists/", closeableHttpClient, jsonObject);
            log.info("responsePost " + responsePost);
//            executePost(baseUri + "/cache/refresh/", closeableHttpClient, null);
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

    private String executePost(String url, HttpClient httpClient, JSONObject requestBody) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        String encoding = Base64.getEncoder().encodeToString((authStr64).getBytes("UTF-8"));
        httpPost.setHeader("Authorization", "Basic " + encoding);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        if (requestBody != null) {
            StringEntity entity = new StringEntity(requestBody.toString(), ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
        }
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity productCatalogEntity = httpResponse.getEntity();
        String strEntity = EntityUtils.toString(productCatalogEntity);
        log.info("method " + httpPost.getMethod());
        log.info("POST uri: " + url + " statusCode " + httpResponse.getStatusLine().getStatusCode());
        log.info("POST uri: " + url + " body " + (requestBody == null ? "" : requestBody.toString()));
        return strEntity;
    }


}
