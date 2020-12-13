package kz.kcell.flow.rfs;

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
@Service("AddElicenseRFS")
public class AddElicense implements JavaDelegate {

    @Value("${asset.url:https://asset.test-flow.kcell.kz}")
    private String assetsUri;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("Insert ELicense // RFS");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();

        SpinJsonNode newTsd = execution.<JsonValue>getVariableTyped("selectedTsd").getValue();

        String tsdId = String.valueOf(newTsd.prop("id"));

        String eLicenseNumber = String.valueOf(execution.getVariable("eLicenseNumber"));
        objectNode.put("elicense_number", eLicenseNumber);

        Calendar c = Calendar.getInstance();
        String eLicenseDate = String.valueOf(execution.getVariable("eLicenseDate"));
        Date eLicenseDateFormatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(eLicenseDate);
        c.setTime(eLicenseDateFormatted);
        c.add(Calendar.HOUR, 6);
        objectNode.put("elicense_date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(c.getTime()));

        ObjectNode rfs_status_id = objectMapper.createObjectNode();
        objectNode.set("rfs_status_id", rfs_status_id);
        rfs_status_id.put("catalog_id", 92);
        rfs_status_id.put("id", 3);

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
            throw new RuntimeException("asset.flow.kcell.kz returns code(insert elicense rfs) " + httpResponse.getStatusLine().getStatusCode());
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
