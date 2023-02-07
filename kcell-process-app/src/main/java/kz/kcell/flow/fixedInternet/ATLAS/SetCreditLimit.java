package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Log
@Service("SetCreditLimit")
public class SetCreditLimit implements JavaDelegate {

    private static final String URL_ENDING = "/customers/set-credit-limit";

    @Value("${atlas.customers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    public void execute(DelegateExecution delegateExecution) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(atlasUrl + URL_ENDING);

        String customerId = (String) delegateExecution.getVariable("account_customerId");

        uriBuilder.setParameter("customerId", customerId);
        uriBuilder.setParameter("getObject", "true");

        JSONObject body = new JSONObject();
        body.put("thresholdBreak", "999999999");
        body.put("changeCreditThresholdReasonId", 0);

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", "Basic " + encoding);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

        StringEntity inputData = new StringEntity(body.toString(), "UTF-8");
        httpPost.setEntity(inputData);

        HttpClient contentProviderHttpClient = HttpClients.createDefault();

        HttpResponse response = contentProviderHttpClient.execute(httpPost);

        if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.info("SetCreditLimit returns code: " + response.getStatusLine().getStatusCode() + "\nMessage: " + EntityUtils.toString(response.getEntity()));
        } else {
            delegateExecution.setVariable("isCreditLimitSet", true);
        }
    }
}