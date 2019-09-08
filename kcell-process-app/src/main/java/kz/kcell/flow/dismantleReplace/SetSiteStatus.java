package kz.kcell.flow.dismantleReplace;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;

@Log
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SetSiteStatus implements JavaDelegate {

    Expression status;
    private final String baseUri;

    @Autowired
    public SetSiteStatus(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String status = this.status.getValue(delegateExecution).toString();
        String siteId = String.valueOf(delegateExecution.getVariable("site"));
        String site_name = String.valueOf(delegateExecution.getVariable("site_name"));

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/sites/" + siteId);
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpGet.addHeader("Referer", baseUri);
        HttpResponse response = httpclient.execute(httpGet);

        log.info("get response code: " + response.getStatusLine().getStatusCode());
        if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300){
            throw new RuntimeException("Site get by id " + siteId + " returns code " + response.getStatusLine().getStatusCode());
        }

        HttpEntity entity = response.getEntity();
        String content = EntityUtils.toString(entity);

        JSONObject site = new JSONObject(content);
        EntityUtils.consume(response.getEntity());

        JSONObject params = site.getJSONObject("params");
        if(params.has("status")){
            JSONObject old_status = params.getJSONObject("status");
            old_status.put(site_name, status);
            params.put("status", old_status);
        } else {
            JSONObject new_status = new JSONObject();
            new_status.put(site_name, status);
            params.put("status", new_status);
        }
        site.put("params", params);

        HttpPut httpPut = new HttpPut(new URI(baseUri + "/asset-management/api/sites/" + siteId));
        httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPut.addHeader("Referer", baseUri);
        StringEntity inputData = new StringEntity(site.toString());
        httpPut.setEntity(inputData);

        CloseableHttpResponse putResponse = httpclient.execute(httpPut);
        log.info("put response code: " + putResponse.getStatusLine().getStatusCode());
        if(putResponse.getStatusLine().getStatusCode() < 200 || putResponse.getStatusLine().getStatusCode() >= 300){
            throw new RuntimeException("Site put by id " + siteId + " returns code " + putResponse.getStatusLine().getStatusCode());
        }

        EntityUtils.consume(putResponse.getEntity());
        putResponse.close();
        httpclient.close();
    }
}
