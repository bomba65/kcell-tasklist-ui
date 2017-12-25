package kz.kcell.flow.sap;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
