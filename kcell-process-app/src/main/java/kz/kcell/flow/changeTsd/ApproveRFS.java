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
@Service("ApproveRFS")
public class ApproveRFS implements JavaDelegate {
    @Value("${asset.url:https://asset.test-flow.kcell.kz}")
    private String assetsUri;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("UpdateRFS");

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();


        Integer newTsdId = Integer.parseInt(String.valueOf(execution.getVariable("newTsdId")));

        String permitResolution = String.valueOf(execution.getVariable("permitResolution"));

        if (permitResolution.equals("keepCurrentRFS")) {
            SpinJsonNode oldTsd = execution.<JsonValue>getVariableTyped("oldTsd").getValue();

            if(oldTsd.hasProp("rfs_date") && oldTsd.prop("rfs_date") != null && oldTsd.prop("rfs_date").value() != null){
                Long timestamp = oldTsd.prop("rfs_date").numberValue().longValue();
                Date date = new Date(timestamp);
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                objectNode.put("rfs_date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(c.getTime()));
            }

            if(oldTsd.hasProp("rfs_status_id") &&  oldTsd.prop("rfs_status_id") != null && oldTsd.prop("rfs_status_id").hasProp("id") && oldTsd.prop("rfs_status_id").prop("id")!=null && oldTsd.prop("rfs_status_id").prop("id").value()!=null){
                SpinJsonNode rfsStatusObj = oldTsd.prop("rfs_status_id");
                Integer rfsStatusId = rfsStatusObj.prop("id").numberValue().intValue();
                ObjectNode rfs_status_id = objectMapper.createObjectNode();
                objectNode.set("rfs_status_id", rfs_status_id);
                rfs_status_id.put("catalog_id", 92);
                rfs_status_id.put("id", rfsStatusId);
            }

            if(oldTsd.hasProp("rfs_number") && oldTsd.prop("rfs_number") != null && oldTsd.prop("rfs_number").value() != null){
                String rfsNumber = oldTsd.prop("rfs_number").stringValue();
                objectNode.put("rfs_number", rfsNumber);
            }
            if(oldTsd.hasProp("elicense_number") && oldTsd.prop("elicense_number") != null && oldTsd.prop("elicense_number").value() != null) {
                String elicenseNumber = oldTsd.prop("elicense_number").stringValue();
                objectNode.put("elicense_number", elicenseNumber);
            }

            if(oldTsd.hasProp("elicense_date") && oldTsd.prop("elicense_date") != null && oldTsd.prop("elicense_date").value() != null) {
                Calendar elicenseCalendar = Calendar.getInstance();
                elicenseCalendar.setTime(new Date(oldTsd.prop("elicense_date").numberValue().longValue()));
                objectNode.put("elicense_date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(elicenseCalendar.getTime()));
            }
        } else if (permitResolution.equals("reissueRFSpermittion")) {
            String rfsNumber = String.valueOf(execution.getVariable("rfsPermitionNumber"));
            objectNode.put("rfs_number", rfsNumber);

            ObjectNode rfs_status_id = objectMapper.createObjectNode();
            objectNode.set("rfs_status_id", rfs_status_id);
            rfs_status_id.put("catalog_id", 92);
            rfs_status_id.put("id", 1);

            Calendar c = Calendar.getInstance();
            String rfsPermitionDate = String.valueOf(execution.getVariable("rfsPermitionDate"));
            Date rfsPermitionDateFormatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(rfsPermitionDate);
            c.setTime(rfsPermitionDateFormatted);
            objectNode.put("rfs_date", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(c.getTime()));
        }


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
            throw new RuntimeException("asset.flow.kcell.kz returns code(rfs approved) " + httpResponse.getStatusLine().getStatusCode());
        }

    }

    private HttpResponse executePut(String url, HttpClient httpClient, String requestBody) throws Exception {
        System.out.println("Call " + url);
        System.out.println("body:  " + requestBody);
        StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        HttpPut httpPut = new HttpPut(url);
        httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpPut.setEntity(entity);
        return httpClient.execute(httpPut);

    }
}
