package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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

@Slf4j
@Service("CreateCorpContract")
public class CreateCorpContract implements JavaDelegate {

    private static final String URL_ENDING = "/create-corp-customer-contract";

    @Value("${atlas.has.url}")
    private String atlasUrl;

    @Value("${atlas.auth}")
    private String atlasAuth;

    @Autowired
    private CloseableHttpClient httpClientWithoutSSL;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(atlasUrl + URL_ENDING);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode contractInfoJson = delegateExecution.hasVariable("contractInfo") ?
            ((JacksonJsonNode) delegateExecution.getVariable("contractInfo")).unwrap() : mapper.createObjectNode();
        String clntId = (String) delegateExecution.getVariable("client_customerId");

        uriBuilder.setParameter("contractNum", contractInfoJson.has("contract") ? contractInfoJson.get("contract").textValue() : "");
        uriBuilder.setParameter("contractCategory", "103");
        uriBuilder.setParameter("signDate", contractInfoJson.has("contract_date") ? contractInfoJson.get("contract_date").textValue() : "");
        uriBuilder.setParameter("expDate", "2999-01-01T00:00:00");
        uriBuilder.setParameter("dealer", "1080");
        uriBuilder.setParameter("invoiceTmp", "205");
        uriBuilder.setParameter("consignee", clntId);
        uriBuilder.setParameter("servInvoice", "Предоставление услуг в области связи");
        uriBuilder.setParameter("payDate", "208");
        uriBuilder.setParameter("baseClnt", clntId);

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", "Basic " + encoding);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

        HttpResponse response = httpClientWithoutSSL.execute(httpPost);

        if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.error("CreateCorpContract, query " + uriBuilder + " returns code " + response.getStatusLine().getStatusCode() + "\n" +
                "Error message: " + EntityUtils.toString(response.getEntity()));
            delegateExecution.setVariable("unsuccessful", true);
        } else {
            log.info("CreateCorpContract, query " + uriBuilder + " returns code " + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(content);

            delegateExecution.setVariable("contract_customerId", jsonObject.getString("clntId"));
            delegateExecution.setVariable("contract_associationId", jsonObject.getString("asscId"));
        }
    }
}
