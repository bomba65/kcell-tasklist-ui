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
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spinjar.com.fasterxml.jackson.databind.JsonNode;
import spinjar.com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.UriBuilder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service("CreateSubscriptionReserveOnetime")
public class CreateSubscriptionReserveOnetime implements JavaDelegate {

    private static final String URL_ENDING = "/subscriptions/reserves";

    @Value("${atlas.subscribers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

    public void execute(DelegateExecution delegateExecution) throws Exception {
        Map<String, String> serviceIdByName = new HashMap();
        serviceIdByName.put("Единовременный платеж за организацию последней мили", "5100");
        serviceIdByName.put("Единовременный платеж за регистрацию блока", "5101");

        ObjectMapper mapper = new ObjectMapper();

        JsonNode timePaymentsJson = delegateExecution.hasVariable("timePayments") ?
            ((JacksonJsonNode) delegateExecution.getVariable("timePayments")).unwrap() : mapper.createObjectNode();

        if (timePaymentsJson.isArray()) {
            for (JsonNode timePayment : timePaymentsJson) {
                if (timePayment.has("amount") && timePayment.get("amount").isNumber() && timePayment.get("amount").doubleValue() > 0) {
                    if (timePayment.has("spec_ont")) {
                        String serviceName = timePayment.get("spec_ont").textValue();
                        Optional<String> nameKey = serviceIdByName.keySet().stream().filter(name -> serviceName.startsWith(name)).findFirst();
                        if (nameKey.isPresent()) {
                            String serviceId = serviceIdByName.get(nameKey.get());

                            UriBuilder uriBuilder = UriBuilder.fromPath(atlasUrl + URL_ENDING);

                            String subscriberId = (String) delegateExecution.getVariable("subscriberId");

                            uriBuilder.queryParam("subscriberId", subscriberId);

                            JSONObject body = new JSONObject();
                            body.put("subscriptionType", "onetime");
                            body.put("productType", "price_Items");
                            // subscription
                            JSONObject subscription = new JSONObject();
                            subscription.put("priceItemId", serviceId);
                            subscription.put("comment", serviceName);
                            // subscription - charge rule
                            JSONObject chargeRule = new JSONObject();
                            chargeRule.put("realTimeControl", true);
                            chargeRule.put("amount", timePayment.get("amount").doubleValue());
                            subscription.put("chargeRule", chargeRule);
                            body.put("subscription", subscription);

                            String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

                            HttpPost httpPost = new HttpPost(uriBuilder.build());
                            httpPost.setHeader("Authorization", "Basic " + encoding);
                            httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

                            StringEntity inputData = new StringEntity(body.toString(), "UTF-8");
                            httpPost.setEntity(inputData);

                            HttpResponse response = httpClientWithoutSSL.execute(httpPost);

                            if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                                log.error("CreateSubscriptionReserveOnetime, query " + uriBuilder + " body " + body + " returns code: " + response.getStatusLine().getStatusCode() + "\n" +
                                    "Error message: " + EntityUtils.toString(response.getEntity()));
                                delegateExecution.setVariable("unsuccessful", true);
                                return;
                            } else {
                                HttpEntity entity = response.getEntity();
                                String entityAsString = EntityUtils.toString(entity);
                                JSONObject jsonObject = new JSONObject(entityAsString);

                                String subscriptionReserveId = jsonObject.getString("subscriptionReserveId");
                                String urlEncodedId = URLEncoder.encode(subscriptionReserveId, StandardCharsets.UTF_8.toString());
                                delegateExecution.setVariable("subscriptionReserveId_" + serviceId, urlEncodedId);
                            }

                        }
                    }
                }

            }
        }

    }
}
