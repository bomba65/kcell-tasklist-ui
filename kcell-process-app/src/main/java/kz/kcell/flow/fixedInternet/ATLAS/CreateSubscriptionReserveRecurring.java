package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.json.JSONArray;
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
@Service("CreateSubscriptionReserveRecurring")
public class CreateSubscriptionReserveRecurring implements JavaDelegate {

    private static final String URL_ENDING = "/subscriptions/reserves";

    @Value("${atlas.subscribers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    private GetTraceNumber getTraceNumber;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

    @Autowired
    public void setGetTraceNumber(GetTraceNumber getTraceNumber) {
        this.getTraceNumber = getTraceNumber;
    }


    public void execute(DelegateExecution delegateExecution) throws Exception {
        Map<String, String> serviceIdByName = new HashMap();
        serviceIdByName.put("Абонентская плата за услугу Фиксированный интернет", "5102");
        serviceIdByName.put("Ежемесячная платеж за организацию последней мили", "5103");
        serviceIdByName.put("Ежемесячный платеж за поддержание блока", "5104");

        ObjectMapper mapper = new ObjectMapper();

        JsonNode monthPaymentsJson = delegateExecution.hasVariable("monthPayments") ?
            ((JacksonJsonNode) delegateExecution.getVariable("monthPayments")).unwrap() : mapper.createObjectNode();

        if (monthPaymentsJson.isArray()) {
            for (JsonNode monthPayment : monthPaymentsJson) {
                if (monthPayment.has("amount_month") && monthPayment.get("amount_month").isNumber() && monthPayment.get("amount_month").doubleValue() > 0) {
                    if (monthPayment.has("spec_month")) {
                        String serviceName = monthPayment.get("spec_month").textValue();
                        Optional<String> nameKey = serviceIdByName.keySet().stream().filter(name -> serviceName.startsWith(name)).findFirst();
                        if (nameKey.isPresent()) {
                            String serviceId = serviceIdByName.get(nameKey.get());
                            getTraceNumber.execute(delegateExecution);
                            if (delegateExecution.hasVariable("traceNumber")) {
                                String traceNumber = (String) delegateExecution.getVariable("traceNumber");
                                delegateExecution.removeVariable("traceNumber");
                                delegateExecution.setVariable("reserveTraceNumber_" + serviceId, traceNumber);

                                UriBuilder uriBuilder = UriBuilder.fromPath(atlasUrl + URL_ENDING);

                                String subscriberId = (String) delegateExecution.getVariable("subscriberId");

                                uriBuilder.queryParam("subscriberId", subscriberId);

                                JSONObject body = new JSONObject();
                                body.put("subscriptionType", "recurring");
                                body.put("productType", "packs");
                                // subscription
                                JSONObject subscription = new JSONObject();
                                subscription.put("packId", serviceId);
                                subscription.put("subscriberPackId", serviceId + "t" + traceNumber);
                                subscription.put("comment", serviceName);
                                // subscription - charge rules
                                JSONArray chargeRules = new JSONArray();
                                JSONObject chargeRule = new JSONObject();
                                chargeRule.put("realTimeControl", true);
                                // subscription - charge rules - charges
                                JSONArray charges = new JSONArray();
                                JSONObject charge = new JSONObject();
                                charge.put("amount", monthPayment.get("amount_month").doubleValue());
                                charges.put(charge);
                                chargeRule.put("charges", charges);
                                chargeRules.put(chargeRule);
                                subscription.put("chargeRules", chargeRules);
                                body.put("subscription", subscription);

                                String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

                                HttpPost httpPost = new HttpPost(uriBuilder.build());
                                httpPost.setHeader("Authorization", "Basic " + encoding);
                                httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

                                StringEntity inputData = new StringEntity(body.toString(), "UTF-8");
                                httpPost.setEntity(inputData);

                                CloseableHttpResponse response = httpClientWithoutSSL.execute(httpPost);

                                if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                                    log.error("CreateSubscriptionReserveRecurring , query " + uriBuilder + " body " + body + " for service " + serviceId + " returns code: " + response.getStatusLine().getStatusCode() + "\n" +
                                        "Error message: " + EntityUtils.toString(response.getEntity()));
                                    delegateExecution.setVariable("unsuccessful", true);
                                    return;
                                } else {
                                    log.info("CreateSubscriptionReserveRecurring , query " + uriBuilder + " body " + body + " for service " + serviceId + " returns code: " + response.getStatusLine().getStatusCode());
                                    HttpEntity entity = response.getEntity();
                                    String entityAsString = EntityUtils.toString(entity);
                                    JSONObject jsonObject = new JSONObject(entityAsString);

                                    String subscriptionReserveId = jsonObject.getString("subscriptionReserveId");
                                    String urlEncodedId = URLEncoder.encode(subscriptionReserveId, StandardCharsets.UTF_8.toString());
                                    delegateExecution.setVariable("subscriptionReserveId_" + serviceId, urlEncodedId);
                                }

                                response.close();
                            }

                        }
                    }
                }

            }
        }

    }
}
