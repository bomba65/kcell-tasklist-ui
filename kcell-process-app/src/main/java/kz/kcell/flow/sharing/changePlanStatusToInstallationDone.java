package kz.kcell.flow.sharing;

import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("changePlanStatusToInstallationDone")
@Log
public class changePlanStatusToInstallationDone implements JavaDelegate {

    private final String baseUri;

    @Autowired
    public changePlanStatusToInstallationDone(@Value("${assets.url:http://localhost}") String assetsUrl) {
        this.baseUri = assetsUrl;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String siteId = String.valueOf(delegateExecution.getVariable("siteId"));
        String newStatus = String.valueOf(delegateExecution.getVariableLocal("newStatus"));


        if(StringUtils.isNotEmpty(siteId)){
            HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/plans/search/changePlanStatus?siteId=" + siteId + "&status=" + newStatus);
            HttpClient httpClient = HttpClients.createDefault();
            HttpResponse httpResponse = httpClient.execute(httpGet);
            delegateExecution.setVariable("sharingPlanStatus", newStatus);
            //log.info("plan change current status Response: " + httpResponse.getStatusLine().getStatusCode());
        }
    }
}
