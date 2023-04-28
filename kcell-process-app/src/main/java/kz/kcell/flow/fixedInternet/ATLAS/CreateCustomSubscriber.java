package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.UriBuilder;
import java.util.Base64;

@Slf4j
@Service("CreateCustomSubscriber")
public class CreateCustomSubscriber implements JavaDelegate {

    private static final String URL_ENDING = "/customers/{account_customerId}/link-custom-subscriber-to-customer";

    @Value("${atlas.customers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

    public void execute(DelegateExecution delegateExecution) throws Exception {
        UriBuilder uriBuilder = UriBuilder.fromPath(atlasUrl + URL_ENDING);

        String customerId = (String) delegateExecution.getVariable("account_customerId");
        String accountNumber = (String) delegateExecution.getVariable("accountNumber");

        JSONObject body = new JSONObject();
        body.put("identification", accountNumber);
        body.put("standardId", 9999);
        JSONObject subscriber = new JSONObject();
        subscriber.put("ratePlanId", 10000);
        body.put("subscriber", subscriber);

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

        HttpPost httpPost = new HttpPost(uriBuilder.build(customerId));
        httpPost.setHeader("Authorization", "Basic " + encoding);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

        StringEntity inputData = new StringEntity(body.toString(), "UTF-8");
        httpPost.setEntity(inputData);

        HttpResponse response = httpClientWithoutSSL.execute(httpPost);

        if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.error("CreateCustomSubscriber returns code: " + response.getStatusLine().getStatusCode() + "\n" +
                "Error message: " + EntityUtils.toString(response.getEntity()));
            delegateExecution.setVariable("unsuccessful", true);
        } else {
            HttpEntity entity = response.getEntity();
            String entityAsString = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(entityAsString);

            delegateExecution.setVariable("subscriberId", jsonObject.getString("subscriberId"));
        }
    }
}
