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
import java.util.Arrays;
import java.util.List;

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
                            if(StringUtils.isEmpty(value.prop("siteLocation").stringValue()) && value.hasProp("filledSiteLocation") && StringUtils.isNotEmpty((value.prop("filledSiteLocation").stringValue()))){

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
                } else{
                    if (tnuSiteLocations.hasProp(site_name)){
                        SpinJsonNode value = tnuSiteLocations.prop(site_name);
                        if(StringUtils.isEmpty(value.prop("siteLocation").stringValue()) && value.hasProp("filledSiteLocation") && StringUtils.isNotEmpty((value.prop("filledSiteLocation").stringValue()))){
                            String tnuSiteLocation = getSiteLocations(site_name);
                            if (tnuSiteLocation == null){
                                setSiteLocation(site_name, String.valueOf(delegateExecution.getVariable("siteName")), value.prop("filledSiteLocation").stringValue());
                                tnuSiteLocation = getSiteLocations(site_name);
                            }

                            if(tnuSiteLocation!=null){
                                value.prop("siteLocation", tnuSiteLocation);
                                delegateExecution.setVariable("sloc", tnuSiteLocation);
                            } else {
                                value.prop("siteLocation", "");
                                hasSloc = "no";
                            }
                        }
                    } else {
                        String tnuSiteLocation = getSiteLocations(site_name);

                        SpinJsonNode value = SpinValues.jsonValue("{}").create().getValue();

                        if(tnuSiteLocation!=null){
                            value.prop("siteLocation", tnuSiteLocation);
                            delegateExecution.setVariable("sloc", tnuSiteLocation);
                        } else {
                            value.prop("siteLocation", "");
                            hasSloc = "no";
                        }
                        value.prop("siteId", String.valueOf(delegateExecution.getVariable("siteName")));
                        value.prop("siteName", site_name);
                        tnuSiteLocations.prop(site_name,value);
                    }
                }
            }

            delegateExecution.setVariable("hasSloc", hasSloc);

            if("yes".equals(hasSloc)){
                for (JsonNode work : jobWorks) {
                    if (work.get("relatedSites").size() > 0) {
                        ArrayNode relatedSites = (ArrayNode) work.get("relatedSites");
                        for (JsonNode relatedSite : relatedSites) {
                            SpinJsonNode value = tnuSiteLocations.prop(relatedSite.get("site_name").asText());

                            SpinJsonNode workForFixedAssetNumber = SpinValues.jsonValue("{}").create().getValue();
                            if(value.hasProp("work")){
                                workForFixedAssetNumber = value.prop("work");
                            }
                            workForFixedAssetNumber.prop(work.get("sapServiceNumber").toString().replace("\"",""), SpinValues.jsonValue("{}").create().getValue());
                            value.prop("work", workForFixedAssetNumber);
                            tnuSiteLocations.prop(relatedSite.get("site_name").asText(), value);
                        }
                    } else {
                        SpinJsonNode value = tnuSiteLocations.prop(site_name);

                        SpinJsonNode workForFixedAssetNumber = SpinValues.jsonValue("{}").create().getValue();
                        if(value.hasProp("work")){
                            workForFixedAssetNumber = value.prop("work");
                        }
                        workForFixedAssetNumber.prop(work.get("sapServiceNumber").toString().replace("\"",""), SpinValues.jsonValue("{}").create().getValue());
                        value.prop("work", workForFixedAssetNumber);
                        tnuSiteLocations.prop(site_name, value);
                    }
                }
            }
            delegateExecution.setVariable("tnuSiteLocations", tnuSiteLocations);
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

    private void fetchWorksDefinitions(DelegateExecution delegateExecution) throws Exception {

        List<String> capexWorks = Arrays.asList("1","2","3","4","5","8","10","11","12","14","15","16","17","19","20","22","23","25","26","28","29","31","32","34",
            "35","36","38","42","45", "46", "47", "48", "49", "50", "54", "55", "56", "57", "60", "62", "65", "66", "71", "72",
            "77", "78", "79", "80", "81", "86", "87", "88", "91", "94", "97", "100", "103", "104", "105", "106", "112", "113",
            "114", "115", "122", "131", "134", "138", "141", "144", "147", "150", "151", "155", "156", "157", "158", "159", "160",
            "161", "162", "165", "168", "169", "172", "173");

        List<String> undefinedWorks = Arrays.asList("39", "40", "41", "43", "61", "63", "67", "68", "73", "74", "82", "84", "85", "89", "92", "95", "98", "101", "107",
            "108", "116", "117", "118", "123", "125", "126", "127", "128", "129", "130", "132", "135", "137", "139", "142", "145",
            "148", "152", "153", "154", "166");

        List<String> opexWorks = Arrays.asList("6", "7", "9", "13", "18", "21", "24", "27", "30", "33", "37", "44", "51", "52", "53", "58", "59", "64", "69", "70", "75",
            "76", "83", "90", "93", "96", "99", "102", "109", "110", "111", "119", "120", "121", "124", "133", "136", "140", "143",
            "146", "149", "163", "164", "167", "170", "171");

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpGet httpGet = new HttpGet(baseUri + "/api/catalogs?v=10");
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
    }
}
