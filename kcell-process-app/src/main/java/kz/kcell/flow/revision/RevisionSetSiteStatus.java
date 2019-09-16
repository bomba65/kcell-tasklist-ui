package kz.kcell.flow.revision;

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
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class RevisionSetSiteStatus implements ExecutionListener {

    Expression status;
    private final String baseUri;

    @Autowired
    public RevisionSetSiteStatus(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {

        boolean isDismantle = false;
        boolean isReplacement = false;

        SpinJsonNode jobWorks = delegateExecution.<JsonValue>getVariableTyped("jobWorks").getValue();
        if(jobWorks.isArray()){
            SpinList<SpinJsonNode> jobWorklist = jobWorks.elements();
            for(SpinJsonNode jobWork : jobWorklist){
                if("9".equals(jobWork.prop("sapServiceNumber").stringValue())){
                    isDismantle = true;
                } else if("8".equals(jobWork.prop("sapServiceNumber").stringValue())){
                    isReplacement = true;
                }
            }
        }

        if(isDismantle || isReplacement){
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

            if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300){
                throw new RuntimeException("Site get by id " + siteId + " returns code " + response.getStatusLine().getStatusCode());
            }

            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);

            JSONObject site = new JSONObject(content);
            EntityUtils.consume(response.getEntity());

            String status = isDismantle?"dismantled":"replaced";

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
            if(putResponse.getStatusLine().getStatusCode() < 200 || putResponse.getStatusLine().getStatusCode() >= 300){
                throw new RuntimeException("Site put by id " + siteId + " returns code " + putResponse.getStatusLine().getStatusCode());
            }

            EntityUtils.consume(putResponse.getEntity());
            putResponse.close();
            httpclient.close();
        }
    }
}
