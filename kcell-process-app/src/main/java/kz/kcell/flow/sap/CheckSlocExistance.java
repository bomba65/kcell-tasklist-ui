package kz.kcell.flow.sap;

import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service("checkSlocExistance")
@Log
public class CheckSlocExistance implements JavaDelegate {

    private final String baseUri;

    @Autowired
    public CheckSlocExistance(@Value("${assets.url:http://localhost}") String assetsUrl) {
        this.baseUri = assetsUrl;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        String siteLocationName = String.valueOf(delegateExecution.getVariable("siteLocationName"));

        log.info("siteLocationName:" + siteLocationName);

        if(StringUtils.isNotEmpty(siteLocationName)){
            String locationUrl = baseUri + "/asset-management/api/locations";
            String siteId = String.valueOf(delegateExecution.getVariable("site"));
            String siteUrl = "http://assets:8080/asset-management/api/sites/" + siteId;

            log.info("{\"params\":{}, \"name\":\"" + siteLocationName + "\",\"site\": \"" + siteUrl + "\"}");

            StringEntity locationInputData = new StringEntity("{\"params\":\"{}\", \"name\":\"" + siteLocationName + "\",\"site\": \"" + siteUrl + "\"}", "UTF-8");

            HttpPost locationHttpPost = new HttpPost(new URI(locationUrl));
            locationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            locationHttpPost.setEntity(locationInputData);

            CloseableHttpClient locationHttpClient = HttpClients.createDefault();
            CloseableHttpResponse locationResponse = locationHttpClient.execute(locationHttpPost);
            log.info("locationResponse code: " + locationResponse.getStatusLine().getStatusCode());
        }

        String site = delegateExecution.getVariable("site").toString();
        HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/locations/search/findBySite?siteId=" + site);
        HttpClient httpClient = HttpClients.createDefault();
        HttpResponse httpResponse = httpClient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();

        String content = EntityUtils.toString(entity);

        JSONObject obj = new JSONObject(content);
        JSONArray locations = obj.getJSONObject("_embedded").getJSONArray("locations");

        if(locations.length()>0){
            JSONObject location = (JSONObject) locations.get(0);

            delegateExecution.setVariable("sloc", (String) location.get("name"));

            delegateExecution.setVariable("hasSloc", "yes");
        } else {
            delegateExecution.setVariable("hasSloc", "no");
        }

        EntityUtils.consume(httpResponse.getEntity());
    }
}
