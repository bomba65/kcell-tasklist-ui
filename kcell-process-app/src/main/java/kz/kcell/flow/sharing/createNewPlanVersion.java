package kz.kcell.flow.sharing;

import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
//import kz.kcell.flow.sharing.createNewPlanVersion;
import java.net.URI;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.impl.client.HttpClients;


@Service("createNewPlanVersion")
@Log
public class createNewPlanVersion implements JavaDelegate {

    private final String baseUri;

    @Autowired
    public createNewPlanVersion(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String sharingPlanStatus = String.valueOf(delegateExecution.getVariable("sharingPlanStatus"));
        String sharingPlanParams = String.valueOf(delegateExecution.getVariable("sharingPlan"));
        String positionNumber = String.valueOf(delegateExecution.getVariable("positionNumber"));
        //String sharingPlanVersion = String.valueOf(delegateExecution.getVariable("sharingPlanVersion"));

        //log.info("sharingPlanStatus:" + sharingPlanStatus);

        HttpGet httpGet = new HttpGet(baseUri + "/directory-management/networkinfrastructure/plan/changePrevCurrentStatus/" + positionNumber);
        httpGet.addHeader("Referer", baseUri);

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();
        HttpResponse httpResponse = httpClient.execute(httpGet);


        if(StringUtils.isNotEmpty(sharingPlanStatus) && StringUtils.isNotEmpty(sharingPlanParams)){
            String plansUrl = baseUri + "/directory-management/networkinfrastructure/plan/createNewPlan";
            //log.info("{\"params\":\"" + sharingPlanParams + "\",\"status\":\"" + sharingPlanStatus + "\",\"site\": \"" + siteUrl + "\",\"is_current\":true}");

            StringEntity planInputData = new StringEntity("{\"params\":" + sharingPlanParams + ",\"status\":\"candidate_sharing\",\"position_number\": " + positionNumber + ",\"is_current\":true}", "UTF-8");

            HttpPost planHttpPost = new HttpPost(new URI(plansUrl));
            planHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            planHttpPost.addHeader("Referer", baseUri);
            planHttpPost.setEntity(planInputData);

            //CloseableHttpClient planHttpClient = HttpClients.createDefault();

            CloseableHttpResponse planResponse = httpClient.execute(planHttpPost);

            log.info("planResponse code: " + planResponse.getStatusLine().getStatusCode());
        }
    }
}
