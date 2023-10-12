package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spinjar.com.fasterxml.jackson.databind.JsonNode;
import spinjar.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service("CreateCorpClient")
public class CreateCorpClient implements JavaDelegate {

    private static final String URL_ENDING = "/create-jural-association";

    @Value("${atlas.has.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    private GetAIRByAddress getAIRByAddress;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

    @Autowired
    public void setGetAIRByAddress(GetAIRByAddress getAIRByAddress) {
        this.getAIRByAddress = getAIRByAddress;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Map<String, Integer> idByOblastName = new HashMap<>();
        idByOblastName.put("Акмолинская Область", 1);
        idByOblastName.put("Актюбинская Область", 2);
        idByOblastName.put("Алматинская Область", 3);
        idByOblastName.put("Атырауская Область", 4);
        idByOblastName.put("Восточно-Казахстанская Область", 5);
        idByOblastName.put("Жамбыльская Область", 6);
        idByOblastName.put("Западно-Казахстанская Область", 7);
        idByOblastName.put("Карагандинская Область", 8);
        idByOblastName.put("Костанайская Область", 9);
        idByOblastName.put("Кызылординская Область", 10);
        idByOblastName.put("Мангистауская Область", 11);
        idByOblastName.put("Павлодарская Область", 12);
        idByOblastName.put("Северо-Казахстанская Область", 13);
        idByOblastName.put("Туркестанская Область", 14);
        idByOblastName.put("Южно-Казахстанская Область", 14);
        idByOblastName.put("г. Астана", 15);
        idByOblastName.put("г. Алматы", 16);
        idByOblastName.put("г. Шымкент", 17);
        idByOblastName.put("ru", 18);

        getAIRByAddress.execute(delegateExecution);

        URIBuilder uriBuilder = new URIBuilder(atlasUrl + URL_ENDING);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode legalInfoJson = delegateExecution.hasVariable("legalInfo") ?
            ((JacksonJsonNode) delegateExecution.getVariable("legalInfo")).unwrap() : mapper.createObjectNode();
        String clientBIN = (String) delegateExecution.getVariable("clientBIN");
        JsonNode resolutionsJson = delegateExecution.hasVariable("resolutions") ?
            ((JacksonJsonNode) delegateExecution.getVariable("resolutions")).unwrap() : mapper.createObjectNode();
        JsonNode contractInfoJson = delegateExecution.hasVariable("contractInfo") ?
            ((JacksonJsonNode) delegateExecution.getVariable("contractInfo")).unwrap() : mapper.createObjectNode();
        String airId = (String) delegateExecution.getVariable("airId");
        JsonNode connectionInfoJson = delegateExecution.hasVariable("connectionInfo") ?
            ((JacksonJsonNode) delegateExecution.getVariable("connectionInfo")).unwrap() : mapper.createObjectNode();


        uriBuilder.setParameter("client", legalInfoJson.has("comp_name") ? legalInfoJson.get("comp_name").textValue() : "");
        uriBuilder.setParameter("bin", clientBIN);
        uriBuilder.setParameter("clientProfile", "1");
        uriBuilder.setParameter("clientType", "14");
        uriBuilder.setParameter("category", "2");
        uriBuilder.setParameter("classClient", "13");
        String comments = "";
        if (resolutionsJson.isArray()) {
            for (JsonNode resolution : resolutionsJson) {
                comments += resolution.has("comment") ? resolution.get("comment").textValue() + "\n" : "";
            }
        }
        uriBuilder.setParameter("comments", comments);
        uriBuilder.setParameter("email", contractInfoJson.has("email") ? contractInfoJson.get("email").textValue() : "");
        uriBuilder.setParameter("phoneNumber", contractInfoJson.has("phone_person") ? contractInfoJson.get("phone_person").textValue() : "");
        uriBuilder.setParameter("aobId", airId != null ? airId : "4633");
        uriBuilder.setParameter("bankname", contractInfoJson.has("bank_name") ? contractInfoJson.get("bank_name").textValue() : "");
        uriBuilder.setParameter("bik", contractInfoJson.has("bik") ? contractInfoJson.get("bik").textValue() : "");
        uriBuilder.setParameter("bankAccount", contractInfoJson.has("IBAN") ? contractInfoJson.get("IBAN").textValue() : "");
        uriBuilder.setParameter("regCertDate", contractInfoJson.has("contract_date") ? contractInfoJson.get("contract_date").textValue() : "");
        uriBuilder.setParameter("budgetOrg", "206");
        uriBuilder.setParameter("segmentId", "1");
        uriBuilder.setParameter("billingGroup", "2");
        if (connectionInfoJson.has("oblast")) {
            uriBuilder.setParameter("brncId", idByOblastName.get(connectionInfoJson.get("oblast").textValue()).toString());
        }
        uriBuilder.setParameter("taxId", "1");
        uriBuilder.setParameter("recCredThreshold", "0");

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));


        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", "Basic " + encoding);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

        CloseableHttpResponse response = httpClientWithoutSSL.execute(httpPost);

        if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.error("CreateCorpClient, query " + uriBuilder + " returns code " + response.getStatusLine().getStatusCode() + "\n" +
                "Error message: " + EntityUtils.toString(response.getEntity()));
            delegateExecution.setVariable("unsuccessful", true);
        } else {
            log.info("CreateCorpClient, query " + uriBuilder + " returns code " + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(content);

            delegateExecution.setVariable("client_customerId", jsonObject.getString("clntId"));
            delegateExecution.setVariable("client_associationId", jsonObject.getString("asscId"));
        }

        response.close();
    }
}
