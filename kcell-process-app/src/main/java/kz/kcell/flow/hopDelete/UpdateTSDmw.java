package kz.kcell.flow.hopDelete;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log
@Service("updateTSDmw")
public class UpdateTSDmw implements JavaDelegate {
    @Value("${asset.url:https://asset.test-flow.kcell.kz}")
    private String assetsUri;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("RFS status change");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        SpinJsonNode newTsd = execution.<JsonValue>getVariableTyped("selectedTsd").getValue();

        String tsdId = String.valueOf(newTsd.prop("id"));

        ObjectNode rfs_status_id = objectMapper.createObjectNode();
        objectNode.set("rfs_status_id", rfs_status_id);
        rfs_status_id.put("catalog_id", 92);
        rfs_status_id.put("id", 4);

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        String path = assetsUri + "/asset-management/tsd_mw/id/" + tsdId + "/nearend_id/%7Bnearend_id%7D/farend_id/%7Bfarend_id%7D";
        HttpResponse httpResponse = executePut(path, httpclient, objectNode.toString());
        String response = EntityUtils.toString(httpResponse.getEntity());
        log.info("json:  ----   " + objectNode.toString());
        log.info("response:  ----   " + response);
        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException("asset.flow.kcell.kz returns code(hop delete) " + httpResponse.getStatusLine().getStatusCode());
        }

    }

    private HttpResponse executePut(String url, HttpClient httpClient, String requestBody) throws Exception {
        StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPut.setEntity(entity);
        return httpClient.execute(httpPut);

    }
}