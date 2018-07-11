package kz.kcell.flow.sap;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service("сalculateWorksTotalLimit")
@Log
public class CalculateWorksTotalLimit implements JavaDelegate {

    private final String baseUri;

    @Autowired
    public CalculateWorksTotalLimit(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Double jobWorksTotal = Double.valueOf(String.valueOf(delegateExecution.getVariable("jobWorksTotal")));

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpGet httpGet = new HttpGet(baseUri + "/directory-management/networkinfrastructure/currency/rate");
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        Double exchangeRate = Double.valueOf(content);

        BigDecimal jobWorksTotalDecimal = new BigDecimal(jobWorksTotal);
        BigDecimal exchangeRateDecimal = new BigDecimal(exchangeRate);

        BigDecimal jobWorksUsdTotal = jobWorksTotalDecimal.divide(exchangeRateDecimal, 2, RoundingMode.DOWN);

        delegateExecution.setVariable("jobWorksUsdTotal", jobWorksUsdTotal.toString());

        EntityUtils.consume(httpResponse.getEntity());
        httpclient.close();

    }
}
