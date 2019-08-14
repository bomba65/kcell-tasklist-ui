package kz.kcell.flow.dismantleReplace;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;

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

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/sites/search/changeSiteStatus?status=" + URLEncoder.encode(status, "UTF-8") + "&siteId=" + siteId);
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        httpGet.addHeader("Referer", baseUri);
        HttpResponse response = httpclient.execute(httpGet);
        httpclient.close();
    }
}
