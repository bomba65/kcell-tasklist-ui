package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
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

import java.util.Base64;

@Slf4j
@Service("GetAIRByAddress")
public class GetAIRByAddress implements JavaDelegate {

    private static final String URL_ENDING = "/customers/AIRaddress";

    @Value("${atlas.customers.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(atlasUrl + URL_ENDING);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = delegateExecution.hasVariable("connectionInfo") ?
            ((JacksonJsonNode) delegateExecution.getVariable("connectionInfo")).unwrap() : mapper.createObjectNode();

        uriBuilder.setParameter("P_REGION", json.has("oblast") ? json.get("oblast").textValue() : "");
        String city = json.has("city") ? json.get("city").textValue() : "";
        city = city.equals("Другой") ? (json.has("extraCity") ? json.get("extraCity").textValue() : "") : city;
        uriBuilder.setParameter("P_CITY", city);
        uriBuilder.setParameter("P_STREET", json.has("add_tech") ? json.get("add_tech").textValue() : "");

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.setHeader("Authorization", "Basic " + encoding);
        CloseableHttpResponse response = httpClientWithoutSSL.execute(httpGet);

        if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.error("GetAIRByAddress, query " + uriBuilder + " returns code " + response.getStatusLine().getStatusCode() + "\n" +
                "Error message: " + EntityUtils.toString(response.getEntity()));
            delegateExecution.setVariable("unsuccessful", true);
        } else {
            log.info("GetAIRByAddress, query " + uriBuilder + " returns code " + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(content);

            delegateExecution.setVariable("airId", jsonObject.getString("id"));
        }

        response.close();
    }
}
