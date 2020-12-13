package kz.kcell.flow.changeTsd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
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
import org.camunda.bpm.engine.delegate.BpmnError;
import org.apache.http.client.HttpClient;
import org.json.JSONObject;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import static org.camunda.spin.Spin.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.*;

@Log
@Service("Informed")
public class Informed implements JavaDelegate {

    @Value("${asset.url:https://asset.test-flow.kcell.kz}")
    private String assetsUri;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Inform Regional Engineer");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        Integer newTsdId = Integer.parseInt(String.valueOf(execution.getVariable("newTsdId")));

        String assigneeName = String.valueOf(execution.getVariable("assigneeName"));
        objectNode.put("review_resp", assigneeName);
        objectNode.put("review_status", true);

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        String path = assetsUri + "/asset-management/tsd_mw/id/" + String.valueOf(newTsdId) + "/nearend_id/%7Bnearend_id%7D/farend_id/%7Bfarend_id%7D";
        HttpResponse httpResponse = executePut(path, httpclient, objectNode.toString());
        String response = EntityUtils.toString(httpResponse.getEntity());
        log.info("json:  ----   " + objectNode.toString());
        log.info("response:  ----   " + response);
        if (httpResponse.getStatusLine().getStatusCode() < 200 || httpResponse.getStatusLine().getStatusCode() >= 300) {
            throw new RuntimeException("asset.flow.kcell.kz returns code(inform) " + httpResponse.getStatusLine().getStatusCode());
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
