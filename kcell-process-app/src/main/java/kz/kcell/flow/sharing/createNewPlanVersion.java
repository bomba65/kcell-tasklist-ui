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

@Service("createNewPlanVersion")
@Log
public class createNewPlanVersion implements JavaDelegate {

    private final String baseUri;

    @Autowired
    public createNewPlanVersion(@Value("${assets.url:http://localhost}") String assetsUrl) {
        this.baseUri = assetsUrl;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String sharingPlanStatus = String.valueOf(delegateExecution.getVariable("sharingPlanStatus"));
        String sharingPlanParams = String.valueOf(delegateExecution.getVariable("sharingPlan"));
        String siteId = String.valueOf(delegateExecution.getVariable("siteId"));
        //String sharingPlanVersion = String.valueOf(delegateExecution.getVariable("sharingPlanVersion"));

        //log.info("sharingPlanStatus:" + sharingPlanStatus);

        if(StringUtils.isNotEmpty(siteId)){
            HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/plans/search/changePrevCurrentStatus?siteId=" + siteId);
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse httpResponse = httpClient.execute(httpGet);
        }

        if(StringUtils.isNotEmpty(sharingPlanStatus) && StringUtils.isNotEmpty(sharingPlanParams)){
            String plansUrl = baseUri + "/asset-management/api/plans";
            String siteUrl = "http://assets:8080/asset-management/api/sites/" + siteId;

            //log.info("{\"params\":\"" + sharingPlanParams + "\",\"status\":\"" + sharingPlanStatus + "\",\"site\": \"" + siteUrl + "\",\"is_current\":true}");

            StringEntity planInputData = new StringEntity("{\"params\":" + sharingPlanParams + ",\"status\":\"candidate_sharing\",\"site\": \"" + siteUrl + "\",\"is_current\":true}", "UTF-8");

            HttpPost planHttpPost = new HttpPost(new URI(plansUrl));
            planHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            planHttpPost.setEntity(planInputData);

            CloseableHttpClient planHttpClient = HttpClients.createDefault();
            CloseableHttpResponse planResponse = planHttpClient.execute(planHttpPost);
            log.info("planResponse code: " + planResponse.getStatusLine().getStatusCode());
        }
    }
}
