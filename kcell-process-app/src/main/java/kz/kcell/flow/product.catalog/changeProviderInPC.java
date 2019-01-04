package kz.kcell.flow.product.catalog;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;


@Service("changeProviderInPC")
@Log
public class changeProviderInPC implements JavaDelegate {

    private final String productCatalogUrl;
    private final String productCatalogAuth;

    @Autowired
    private Environment environment;

    //private final String baseUri;

    @Autowired
    public changeProviderInPC(@Value("${product.catalog.url:http://ldb-al-preprod.kcell.kz}") String productCatalogUrl, @Value("${product.catalog.auth:app.camunda.user:Asd123Qwerty!}") String productCatalogAuth) {
        this.productCatalogUrl = productCatalogUrl;
        this.productCatalogAuth = productCatalogAuth;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
        Boolean productCatalogSuccessfulResponse = true;

        String connectionType = String.valueOf(delegateExecution.getVariable("connectionType"));
        String provider = String.valueOf(delegateExecution.getVariable("provider"));
        JSONArray identifierJSONArray = new JSONArray(String.valueOf(delegateExecution.getVariable("identifiers")));
        JSONObject identifierJSON = identifierJSONArray.getJSONObject(0);

        JSONArray operatorJSONArray = identifierJSON.getJSONArray("operators");
        String title = String.valueOf(identifierJSON.get("title"));
        String operatorType =  String.valueOf(delegateExecution.getVariable("operatorType"));

        JSONObject requestBodyJSON = new JSONObject();
        // put variables into json request body
        //requestBodyJSON.put("id", 99999);
        requestBodyJSON.put("provider", provider);



        if (isSftp) {
            //System.out.println("requestBodyJSON: " + requestBodyJSON.toString() + ", operators: " + operatorJSONArray.toString());

            String encoding = Base64.getEncoder().encodeToString((productCatalogAuth).getBytes("UTF-8"));

            StringEntity providerData = new StringEntity(requestBodyJSON.toString(), ContentType.APPLICATION_JSON);


            HttpPost providerPost = new HttpPost(new URI(productCatalogUrl+"/vas_content_providers"));
            providerPost.setHeader("Authorization", "Basic " + encoding);
            providerPost.addHeader("Content-Type", "application/json;charset=UTF-8");

            providerPost.setEntity(providerData);

            HttpClient contentProviderHttpClient = HttpClients.createDefault();

            HttpResponse contentProviderResponse = contentProviderHttpClient.execute(providerPost);

            HttpEntity entity = contentProviderResponse.getEntity();
            String responseBodyString = EntityUtils.toString(entity);


            JsonValue requestJsonValue = SpinValues.jsonValue(requestBodyJSON.toString()).create();
            delegateExecution.setVariable("productCatalogRequest", requestJsonValue);

            JsonValue responseJsonValue = SpinValues.jsonValue(responseBodyString).create();
            delegateExecution.setVariable("productCatalogResponse", responseJsonValue);

            delegateExecution.setVariable("productCatalogSuccessfulResponse", productCatalogSuccessfulResponse);

        } else {
            JsonValue requestJsonValue = SpinValues.jsonValue(requestBodyJSON.toString()).create();
            delegateExecution.setVariable("productCatalogRequest", requestJsonValue);

            // put some data into json request body to response with request data + data you put
            //requestBodyJSON.put("id", 99999);

            JsonValue responseJsonValue = SpinValues.jsonValue(requestBodyJSON.toString()).create();
            delegateExecution.setVariable("productCatalogResponse", responseJsonValue);

            delegateExecution.setVariable("productCatalogSuccessfulResponse", productCatalogSuccessfulResponse);
        }
    }
}
