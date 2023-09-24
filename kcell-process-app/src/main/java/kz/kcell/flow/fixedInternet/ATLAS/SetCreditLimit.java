package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service("SetCreditLimit")
public class SetCreditLimit implements JavaDelegate {

    private static final String URL_ENDING = "/customers/set-credit-limit";

    @Value("${atlas.customers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

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

        HttpResponse response = httpClientWithoutSSL.execute(httpPost);

        if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.error("SetCreditLimit, query " + uriBuilder + " body " + body + " returns code: " + response.getStatusLine().getStatusCode() + "\n" +
                "Error message: " + EntityUtils.toString(response.getEntity()));
            delegateExecution.setVariable("unsuccessful", true);
        } else {
            log.info("SetCreditLimit, query " + uriBuilder + " body " + body + " returns code: " + response.getStatusLine().getStatusCode());
            delegateExecution.setVariable("isCreditLimitSet", true);
        }
    }
}
