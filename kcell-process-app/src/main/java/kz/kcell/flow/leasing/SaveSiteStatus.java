package kz.kcell.flow.leasing;

import kz.kcell.flow.files.Minio;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.*;
import java.util.Date;
import java.util.TimeZone;

import static org.camunda.spin.Spin.JSON;

@Log
@Service("SaveSiteStatus")
public class SaveSiteStatus implements JavaDelegate {

    @Autowired
    DataSource dataSource;

    private String assetsUri;
    private String baseUri;

    @Autowired
    public SaveSiteStatus(@Value("${mail.message.baseurl:http://localhost}") String baseUri, @Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUri) {
        this.baseUri = baseUri;
        this.assetsUri = assetsUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Long assetsCreatedSiteId = delegateExecution.getVariable("assetsCreatedSiteId") != null ? Long.valueOf(delegateExecution.getVariable("assetsCreatedSiteId").toString()) : null;

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        JSONObject value = new JSONObject();
        value.put("id", assetsCreatedSiteId);

        JSONObject site_status_id_json = new JSONObject();
        site_status_id_json.put("catalog_id", 3);
        site_status_id_json.put("id", 1);
        value.put("site_status_id", site_status_id_json);

        log.info("body value.toString(): ");
        log.info(value.toString());

        HttpPut httpPut = new HttpPut(new URI(this.assetsUri + "/asset-management/sites/id/" + assetsCreatedSiteId));
        httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPut.addHeader("Referer", baseUri);
        StringEntity inputData = new StringEntity(value.toString());
        httpPut.setEntity(inputData);

        CloseableHttpResponse postResponse = httpclient.execute(httpPut);
        HttpEntity entity = postResponse.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        JSONObject jsonResponse = new JSONObject(responseString);

        log.info("put response code: " + postResponse.getStatusLine().getStatusCode());
        if (postResponse.getStatusLine().getStatusCode() < 200 || postResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException("Candidate post returns code " + postResponse.getStatusLine().getStatusCode());
        }
    }
}
