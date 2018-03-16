package kz.kcell.flow.sap;

import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
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
    public CheckSlocExistance(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {


        String siteRegion = String.valueOf(delegateExecution.getVariable("siteRegion"));
        String contractor = String.valueOf(delegateExecution.getVariable("contractor"));
        String reason = String.valueOf(delegateExecution.getVariable("reason"));
        String jrNumber = String.valueOf(delegateExecution.getVariable("jrNumber"));

        Integer counter = 1;
        if(delegateExecution.getVariable("sapTryCounter")!=null){
            counter = Integer.valueOf(String.valueOf(delegateExecution.getVariable("sapTryCounter"))) + 1;
        }
        delegateExecution.setVariable("sapTryCounter", counter);

        String sapJrNumber = (siteRegion.equals("astana")?"ast":siteRegion) + "-"
            + contractor + "-"
            + reason + "-"
            + jrNumber.substring(jrNumber.length()-7,jrNumber.length()) + "-"
            + (String.valueOf(counter).length()<2 ?"0":"") + String.valueOf(counter);
        delegateExecution.setVariable("sapJrNumber", sapJrNumber);

        // astana-4-3-18-5001-01
        // astana-lse-S&FM-18-5001

        log.info("sapTryCounter: " + counter);
        log.info("sapJrNumber: " + sapJrNumber);

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        String siteLocationName = String.valueOf(delegateExecution.getVariable("siteLocationName"));

        log.info("siteLocationName:" + siteLocationName);

        if(siteLocationName!=null && StringUtils.isNotEmpty(siteLocationName.trim())){
            String locationUrl = baseUri + "/asset-management/api/locations";
            String siteId = String.valueOf(delegateExecution.getVariable("site"));
            String siteUrl = baseUri + "/asset-management/api/sites/" + siteId;

            log.info("{\"params\":{}, \"name\":\"" + siteLocationName + "\",\"site\": \"" + siteUrl + "\"}");

            StringEntity locationInputData = new StringEntity("{\"params\":\"{}\", \"name\":\"" + siteLocationName + "\",\"site\": \"" + siteUrl + "\"}", "UTF-8");

            HttpPost locationHttpPost = new HttpPost(new URI(locationUrl));
            locationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            locationHttpPost.setEntity(locationInputData);

            CloseableHttpResponse locationResponse = httpclient.execute(locationHttpPost);
            log.info("locationResponse code: " + locationResponse.getStatusLine().getStatusCode());
        }

        String site = delegateExecution.getVariable("site").toString();
        HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/locations/search/findBySite?siteId=" + site);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();

        String content = EntityUtils.toString(entity);

        JSONObject obj = new JSONObject(content);
        JSONArray locations = obj.getJSONObject("_embedded").getJSONArray("locations");

        log.info("locations.length(): " + locations.length());

        if(locations.length()>0){
            log.info("locations.get(0): " + locations.get(0));

            JSONObject location = (JSONObject) locations.get(0);
            log.info("location.get(name): " + location.get("name"));

            if(location.get("name")!=null && !"null".equals((String) location.get("name"))){
                delegateExecution.setVariable("sloc", (String) location.get("name"));
                delegateExecution.setVariable("hasSloc", "yes");
            } else {
                delegateExecution.setVariable("hasSloc", "no");
            }
        } else {
            delegateExecution.setVariable("hasSloc", "no");
        }

        EntityUtils.consume(httpResponse.getEntity());
    }
}
