package kz.kcell.flow.revision;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service("sendDataAssetsDb")
@Log
public class SendDataAssetsDb implements JavaDelegate {

    private final String assetsUri;

    @Autowired
    public SendDataAssetsDb(@Value("${asset.url:https://asset.test-flow.kcell.kz}") String assetsUri) {
        this.assetsUri = assetsUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        if (delegateExecution.getVariable("isSiteFromAssets") != null && delegateExecution.getVariable("isSiteFromAssets").toString().equals("true")) {
            boolean isDismantle = false;

            SpinJsonNode jobWorks = delegateExecution.<JsonValue>getVariableTyped("jobWorks").getValue();
            if (jobWorks.isArray()) {
                SpinList<SpinJsonNode> jobWorklist = jobWorks.elements();
                for (SpinJsonNode jobWork : jobWorklist) {
                    if ("2.4".equals(jobWork.prop("sapServiceNumber").stringValue())) {
                        isDismantle = true;
                    }
                }
            }
            if (isDismantle) {

                String siteId = String.valueOf(delegateExecution.getVariable("site"));
                String site_name = String.valueOf(delegateExecution.getVariable("site_name"));

                SSLContextBuilder builder = new SSLContextBuilder();
                builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
                CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

                JSONObject jsonObject = new JSONObject();
                JSONObject status = new JSONObject();
                status.put("id", 6);
                status.put("catalog_id", 3);
                JSONObject subStatus = new JSONObject();
                status.put("id", 10);
                status.put("catalog_id", 85);
                jsonObject.put("site_status_id", status);
                jsonObject.put("site_substatus_id", subStatus);

                HttpPut httpPut = new HttpPut(new URI(assetsUri + "/asset-management/sites/id/" + siteId));
                httpPut.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpPut.addHeader("Referer", assetsUri);
                StringEntity inputData = new StringEntity(jsonObject.toString());
                httpPut.setEntity(inputData);

                CloseableHttpResponse putResponse = httpClient.execute(httpPut);
                if (putResponse.getStatusLine().getStatusCode() < 200 || putResponse.getStatusLine().getStatusCode() >= 300) {
                    throw new RuntimeException("Site put by id " + siteId + " returns code " + putResponse.getStatusLine().getStatusCode());
                }

                EntityUtils.consume(putResponse.getEntity());
                putResponse.close();
            }
        }
    }
}
