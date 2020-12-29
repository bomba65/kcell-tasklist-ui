package kz.kcell.flow.tnuTsd;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log
@Service("MapVariablesGetRfs")
public class MapVariablesGetRfs implements JavaDelegate {

    @Value("${asset.url:https://asset.test-flow.kcell.kz}")
    private String assetsUri;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        try (CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build()) {
            String id = delegateExecution.getVariable("tsdMwId").toString();
            String content = executeGet(assetsUri + "/asset-management/tsd_mw?id=" + id, httpclient);

            JSONArray array = new JSONArray(content);
            JSONObject obj = new JSONObject();

            if (array.length() > 0) {
                obj = array.getJSONObject(0);
                delegateExecution.setVariable("selectedTsd", SpinValues.jsonValue(obj.toString()).create());
            } else {
                throw new Exception("Last inserted tsd with ID -" + id + " not fetched");
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception(e.getMessage());
        }
        delegateExecution.setVariable("startedManually", false);

    }

    private String executeGet(String url, HttpClient httpClient) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity responseEntity = httpResponse.getEntity();
        log.info("GET uri: " + url + " statusCode " + httpResponse.getStatusLine().getStatusCode());
        return EntityUtils.toString(responseEntity);
    }
}
