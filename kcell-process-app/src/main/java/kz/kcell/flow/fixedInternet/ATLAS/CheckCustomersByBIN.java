package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Log
@Service("CheckCustomersByBIN")
public class CheckCustomersByBIN implements JavaDelegate {

    private static final String URL_ENDING = "/customers/search";

    @Value("${atlas.customers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(atlasUrl + URL_ENDING);

        String clientBIN = (String)delegateExecution.getVariable("clientBIN");

        uriBuilder.setParameter("iin", clientBIN);
        uriBuilder.setParameter("includeClosedSubscribers", "false");
        uriBuilder.setParameter("page", "0");
        uriBuilder.setParameter("size", "20");

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

        CloseableHttpClient httpclient = HttpClients.custom().build();

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", "Basic " + encoding);
        HttpResponse response = httpclient.execute(httpGet);

        if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.info("CheckCustomersByBIN returns code " + response.getStatusLine().getStatusCode() + "\nMessage: " + EntityUtils.toString(response.getEntity()));
        } else {
            HttpEntity entity = response.getEntity();
            String entityAsString = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(entityAsString);

            JSONArray content = jsonObject.getJSONArray("content");
            if (content.length() > 0) {
                String customerId = content.getJSONObject(0).getString("customerId");
                delegateExecution.setVariable("client_customerId", customerId);
            }
        }

    }
}
