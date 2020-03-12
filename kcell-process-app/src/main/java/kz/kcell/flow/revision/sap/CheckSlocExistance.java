package kz.kcell.flow.revision.sap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
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

        ObjectMapper mapper = new ObjectMapper();

        String reason = String.valueOf(delegateExecution.getVariable("reason"));
        String jrNumber = String.valueOf(delegateExecution.getVariable("jrNumber"));

        String site_name = delegateExecution.getVariable("site_name").toString();

        if("2".equals(reason)) {
            String hasSloc = "yes";

            SpinJsonNode tnuSiteLocations = SpinValues.jsonValue("{}").create().getValue();
            if (delegateExecution.hasVariable("tnuSiteLocations")){
                tnuSiteLocations = delegateExecution.<JsonValue>getVariableTyped("tnuSiteLocations").getValue();
            }

            ArrayNode jobWorks = (ArrayNode) mapper.readTree(delegateExecution.getVariable("jobWorks").toString());
            for (JsonNode work : jobWorks) {
                if (work.get("relatedSites").size()>0) {
                    ArrayNode relatedSites = (ArrayNode) work.get("relatedSites");
                    for (JsonNode relatedSite : relatedSites) {
                        if (tnuSiteLocations.hasProp(relatedSite.get("site_name").asText())){
                            SpinJsonNode value = tnuSiteLocations.prop(relatedSite.get("site_name").asText());
                            if(StringUtils.isEmpty(value.prop("siteLocation").stringValue()) && StringUtils.isNotEmpty((value.prop("filledSiteLocation").stringValue()))){

                                String tnuSiteLocation = getSiteLocations(relatedSite.get("site_name").asText());
                                if (tnuSiteLocation == null){
                                    setSiteLocation(relatedSite.get("site_name").asText(), relatedSite.get("name").asText(), value.prop("filledSiteLocation").stringValue());
                                    tnuSiteLocation = getSiteLocations(relatedSite.get("site_name").asText());
                                }

                                if(tnuSiteLocation!=null){
                                    value.prop("siteLocation", tnuSiteLocation);
                                    if(site_name.equals(relatedSite.get("site_name").asText())){
                                        delegateExecution.setVariable("sloc", tnuSiteLocation);
                                    }
                                } else {
                                    value.prop("siteLocation", "");
                                    hasSloc = "no";
                                }
                            }
                        } else {
                            String tnuSiteLocation = getSiteLocations(relatedSite.get("site_name").asText());

                            SpinJsonNode value = SpinValues.jsonValue("{}").create().getValue();

                            if(tnuSiteLocation!=null){
                                value.prop("siteLocation", tnuSiteLocation);
                                if(site_name.equals(relatedSite.get("site_name").asText())){
                                    delegateExecution.setVariable("sloc", tnuSiteLocation);
                                }
                            } else {
                                value.prop("siteLocation", "");
                                hasSloc = "no";
                            }
                            value.prop("siteId", relatedSite.get("name").asText());
                            value.prop("siteName", relatedSite.get("site_name").asText());
                            tnuSiteLocations.prop(relatedSite.get("site_name").asText(),value);
                        }
                    }
                }
            }

            delegateExecution.setVariable("tnuSiteLocations", tnuSiteLocations);
            delegateExecution.setVariable("hasSloc", hasSloc);

        } else {
            String siteLocationName = null;
            if(delegateExecution.getVariable("siteLocationName") != null){
                siteLocationName = String.valueOf(delegateExecution.getVariable("siteLocationName"));
            }

            log.info("jrNumber: " + jrNumber + " siteLocationName:" + siteLocationName);

            String siteLocation = getSiteLocations(site_name);

            if(StringUtils.isNotEmpty(siteLocationName) && siteLocation == null){
                setSiteLocation(String.valueOf(delegateExecution.getVariable("site_name")), String.valueOf(delegateExecution.getVariable("siteName")), siteLocationName);
                siteLocation = getSiteLocations(site_name);
            }
            if(siteLocation!=null){
                delegateExecution.setVariable("sloc", siteLocation);
                delegateExecution.setVariable("hasSloc", "yes");
            } else {
                delegateExecution.setVariable("hasSloc", "no");
            }
        }

        fetchWorksDefinitions(delegateExecution);
    }

    private String getSiteLocations(String site_name) throws Exception{
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/locations/search/findBySiteName?sitename=" + site_name);
        httpGet.addHeader("Referer", baseUri);
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        JSONObject obj = new JSONObject(content);
        EntityUtils.consume(httpResponse.getEntity());
        httpclient.close();

        JSONArray locations = obj.getJSONObject("_embedded").getJSONArray("locations");
        log.info("site name: " + site_name + " locations.length(): " + locations.length());

        if(locations.length()>0){
            log.info("locations.get(0): " + locations.get(0));

            JSONObject location = (JSONObject) locations.get(0);
            log.info("location.get(name): " + location.get("name"));

            if(location.get("name")!=null && !"null".equals((String) location.get("name"))){
                return (String) location.get("name");
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

    private void setSiteLocation(String site_name, String siteId, String siteLocationName) throws Exception{
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        String locationUrl = baseUri + "/asset-management/api/locations";

        log.info("{\"params\":{}, \"name\":\"" + siteLocationName + "\",\"sitename\": \"" + site_name + "\",\"siteId\": \"" + siteId + "\"}");

        StringEntity locationInputData = new StringEntity("{\"params\":\"{}\", \"name\":\"" + siteLocationName + "\",\"sitename\": \"" + site_name + "\",\"siteId\": \"" + siteId + "\"}", "UTF-8");

        HttpPost locationHttpPost = new HttpPost(new URI(locationUrl));
        locationHttpPost.addHeader("Content-Type", "application/json;charset=UTF-8");
        locationHttpPost.addHeader("Referer", baseUri);
        locationHttpPost.setEntity(locationInputData);

        CloseableHttpResponse locationResponse = httpclient.execute(locationHttpPost);
        log.info("locationResponse code: " + locationResponse.getStatusLine().getStatusCode());

        EntityUtils.consume(locationResponse.getEntity());
        httpclient.close();
    }

    private void fetchWorksDefinitions(DelegateExecution delegateExecution) throws Exception{
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpGet httpGet = new HttpGet(baseUri + "/api/catalogs?force=2");
        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
        HttpResponse httpResponse = httpclient.execute(httpGet);

        HttpEntity entity = httpResponse.getEntity();
        String content = EntityUtils.toString(entity);

        JSONObject obj = new JSONObject(content);
        EntityUtils.consume(httpResponse.getEntity());
        httpclient.close();

        JSONArray definitions = obj.getJSONArray("works");

        JsonValue jobWorksObj = delegateExecution.<JsonValue>getVariableTyped( "jobWorks");
        SpinList<SpinJsonNode> jobWorks = jobWorksObj.getValue().elements();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode workDefinitionMap = mapper.createObjectNode();

        for (SpinJsonNode work:jobWorks){
            for(int i=0; i<definitions.length();i++){
                JSONObject def = (JSONObject) definitions.get(i);

                if(work.prop("sapServiceNumber").toString().replace("\"","").equals(def.getString("sapServiceNumber"))){
                    if(!workDefinitionMap.has(def.getString("sapServiceNumber"))){
                        workDefinitionMap.set(def.getString("sapServiceNumber"), mapper.readTree(def.toString()));
                    }
                    break;
                }
            }
        }
        JsonValue jsonValue = SpinValues.jsonValue(workDefinitionMap.toString()).create();
        delegateExecution.setVariable("workDefinitionMap", jsonValue);

        log.info(workDefinitionMap.toString());
    }
}
