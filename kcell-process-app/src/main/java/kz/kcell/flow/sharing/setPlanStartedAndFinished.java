package kz.kcell.flow.sharing;

import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service("setPlanStartedAndFinished")
@Log
public class setPlanStartedAndFinished implements JavaDelegate {

    private final String baseUri;

    @Autowired
    public setPlanStartedAndFinished(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String positionNumber = String.valueOf(delegateExecution.getVariable("positionNumber"));
        String sharingPlanStatus = String.valueOf(delegateExecution.getVariable("sharingPlanStatus"));
        Boolean startAndFinish = Boolean.parseBoolean(delegateExecution.getVariable("startAndFinish").toString());

        System.out.println("setPlanStartedAndFinished position_number: " + positionNumber + ", status: " + sharingPlanStatus + ", startAndFinish: " + startAndFinish);

        if(StringUtils.isNotEmpty(positionNumber) && sharingPlanStatus.equals("site_sharing_complete")){
            HttpGet httpGet = new HttpGet(baseUri + "/directory-management/networkinfrastructure//plan/setStartedAndFinished/" + positionNumber);
            httpGet.addHeader("Referer", baseUri);

            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build());
            CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            delegateExecution.setVariable("startAndFinish", false);
            //log.info("plan change current status Response: " + httpResponse.getStatusLine().getStatusCode());
        }
    }
}
