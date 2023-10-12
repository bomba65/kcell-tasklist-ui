package kz.kcell.flow.fixedInternet.ATLAS;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
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

import java.util.Base64;

@Slf4j
@Service("CreateAccount")
public class CreateAccount implements JavaDelegate {

    private static final String URL_ENDING = "/customers/registration";

    @Value("${atlas.customers.url}")
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
        getAIRByAddress.execute(delegateExecution);

        URIBuilder uriBuilder = new URIBuilder(atlasUrl + URL_ENDING);
        ObjectMapper mapper = new ObjectMapper();

        JsonNode legalInfoJson = delegateExecution.hasVariable("legalInfo") ?
            ((JacksonJsonNode) delegateExecution.getVariable("legalInfo")).unwrap() : mapper.createObjectNode();
        String associationId = (String) delegateExecution.getVariable("contract_associationId");
        String clientBIN = (String) delegateExecution.getVariable("clientBIN");
        String airId = (String) delegateExecution.getVariable("airId");
        JsonNode connectionInfoJson = delegateExecution.hasVariable("connectionInfo") ?
            ((JacksonJsonNode) delegateExecution.getVariable("connectionInfo")).unwrap() : mapper.createObjectNode();
        JsonNode responsiblePersonsInfoJson = delegateExecution.hasVariable("responsiblePersonsInfo") ?
            ((JacksonJsonNode) delegateExecution.getVariable("responsiblePersonsInfo")).unwrap() : mapper.createObjectNode();

        JSONObject body = new JSONObject();

        body.put("name", legalInfoJson.has("comp_name") ? legalInfoJson.get("comp_name").textValue() : "");
        body.put("juralTypeId", 2);
        body.put("customerProfileId", 1);
        body.put("registrationCategoryId", 3);
        body.put("customerTypeId", 14);
        body.put("customerClassId", 13);
        body.put("statusId", 2);
        body.put("categoryId", 2);
        // financial info
        JSONObject financialInfo = new JSONObject();
        financialInfo.put("billingGroupId", 2);
        JSONArray customerTaxSchemes = new JSONArray();
        JSONObject taxSchemeId = new JSONObject();
        taxSchemeId.put("taxSchemeId", 1);
        customerTaxSchemes.put(taxSchemeId);
        financialInfo.put("customerTaxSchemes", customerTaxSchemes);
        body.put("financialInfo", financialInfo);
        // association
        JSONObject association = new JSONObject();
        association.put("associationId", associationId);
        body.put("association", association);
        // personal data
        JSONObject personalData = new JSONObject();
        personalData.put("INN", clientBIN);
        // personal data - fact address
        JSONObject factAddress = new JSONObject();
        factAddress.put("addressId", airId);
        String address = "";
        address += connectionInfoJson.has("oblast") ? connectionInfoJson.get("oblast").textValue() + ", " : "";
        String city = connectionInfoJson.has("city") ? connectionInfoJson.get("city").textValue() : "";
        city = city.equals("Другой") ? (connectionInfoJson.has("extraCity") ? connectionInfoJson.get("extraCity").textValue() : "") : city;
        address += city.equals("") ? city : city + ", ";
        address += connectionInfoJson.has("add_tech") ? connectionInfoJson.get("add_tech").textValue() : "";
        factAddress.put("addressAsString", address);
        factAddress.put("appartment", JSONObject.NULL);
        factAddress.put("addressZIP", JSONObject.NULL);
        factAddress.put("house", JSONObject.NULL);
        factAddress.put("block", JSONObject.NULL);
        factAddress.put("street", JSONObject.NULL);
        factAddress.put("districtOfCity", JSONObject.NULL);
        factAddress.put("province", JSONObject.NULL);
        factAddress.put("districtOfProvince", JSONObject.NULL);
        // personal data - fact address - city
        JSONObject cityJson = new JSONObject();
        cityJson.put("cityAsText", city);
        factAddress.put("city", cityJson);
        // personal data - fact address - country
        JSONObject country = new JSONObject();
        country.put("countryId", 1);
        factAddress.put("country", country);
        personalData.put("factAddress", factAddress);
        // personal data - delivery info
        JSONObject deliveryInfo = new JSONObject();
        deliveryInfo.put("name", legalInfoJson.has("comp_name") ? legalInfoJson.get("comp_name").textValue() : "");
        deliveryInfo.put("deliveryTypeId", 3);
        deliveryInfo.put("phone", responsiblePersonsInfoJson.has("tel_spec") ? responsiblePersonsInfoJson.get("tel_spec").textValue() : "");
        // personal data - delivery info - main contact person
        JSONObject mainContactPerson = new JSONObject();
        mainContactPerson.put("phone", responsiblePersonsInfoJson.has("tel_spec") ? responsiblePersonsInfoJson.get("tel_spec").textValue() : "");
        deliveryInfo.put("mainContactPerson", mainContactPerson);
        personalData.put("deliveryInfo", deliveryInfo);
        body.put("personalData", personalData);

        String encoding = Base64.getEncoder().encodeToString((atlasAuth).getBytes("UTF-8"));

        HttpPost httpPost = new HttpPost(uriBuilder.build());
        httpPost.setHeader("Authorization", "Basic " + encoding);
        httpPost.addHeader("Content-Type", "application/json;charset=UTF-8");

        StringEntity inputData = new StringEntity(body.toString(), "UTF-8");
        httpPost.setEntity(inputData);

        CloseableHttpResponse response = httpClientWithoutSSL.execute(httpPost);

        if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
            log.error("CreateAccount, query " + uriBuilder + " body " + body + " returns code: " + response.getStatusLine().getStatusCode() + "\n" +
                "Error message: " + EntityUtils.toString(response.getEntity()));
            delegateExecution.setVariable("unsuccessful", true);
        } else {
            log.info("CreateAccount, query " + uriBuilder + " body " + body + " returns code: " + response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            JSONObject jsonObject = new JSONObject(content);

            delegateExecution.setVariable("account_customerId", jsonObject.getString("customerId"));
            delegateExecution.setVariable("accountNumber", jsonObject.getString("customerAccountNumber"));
        }

        response.close();
    }
}
