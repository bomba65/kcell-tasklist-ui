package kz.kcell.flow.sao;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;


@Service("PBXClientSAO")
@Log
public class PBXClientUpdate implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String saoUrl;
    private final String b2bCRMurl;
    private final String b2bCRMauth;

    @Autowired
    public PBXClientUpdate(@Value("${sao.api.url:http://sao.kcell.kz/apis}") String saoUrl,
                           @Value("${b2b.crm.url:http://ldb-al.kcell.kz/corp_client_profile/bin/}") String b2bCRMurl,
                           @Value("${b2b.crm.auth:app.camunda.user:Asd123Qwerty!}") String b2bCRMauth) {
        this.saoUrl = saoUrl;
        this.b2bCRMurl = b2bCRMurl;
        this.b2bCRMauth = b2bCRMauth;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
//        Boolean isSftp = false;

        JSONObject customerInformationJSON = new JSONObject(String.valueOf(delegateExecution.getVariable("customerInformation")));
        JSONObject technicalSpecificationsJSON = new JSONObject(String.valueOf(delegateExecution.getVariable("technicalSpecifications")));
        JSONObject sipProtocolJSON = new JSONObject(String.valueOf(delegateExecution.getVariable("sipProtocol")));
        String fkClient = String.valueOf(delegateExecution.getVariable("fk_client"));

        JSONObject saoRequest = new JSONObject();

        if (isSftp && fkClient != null) {
            String encoding = Base64.getEncoder().encodeToString((b2bCRMauth).getBytes("UTF-8"));

            CloseableHttpClient productCatalogHttpClient = HttpClients.custom().build();

            HttpGet httpGet = new HttpGet(b2bCRMurl + customerInformationJSON.get("bin").toString());
            httpGet.setHeader("Authorization", "Basic " + encoding);

            HttpResponse httpResponse = productCatalogHttpClient.execute(httpGet);

            HttpEntity productCatalogEntity = httpResponse.getEntity();
            String productCatalogContent = EntityUtils.toString(productCatalogEntity);

            JSONObject productCatalogClientJSON = new JSONObject(productCatalogContent);

            EntityUtils.consume(httpResponse.getEntity());
            productCatalogHttpClient.close();

            JsonValue productCatalogClientJsonValue = SpinValues.jsonValue(productCatalogContent).create();
            delegateExecution.setVariable("productCatalogClientResponse", productCatalogClientJsonValue);

            if(productCatalogClientJSON.has("id")) {
                Integer corpClientId = (Integer) productCatalogClientJSON.get("id");
                if(corpClientId > 0) {
                    List<NameValuePair> params = new ArrayList<NameValuePair>();

                    params.add(new BasicNameValuePair("fk_client", fkClient));
                    params.add(new BasicNameValuePair("status", "21"));
                    params.add(new BasicNameValuePair("tic_name", customerInformationJSON.get("ticName").toString()));
                    params.add(new BasicNameValuePair("comp_name", customerInformationJSON.get("legalName").toString()));
                    params.add(new BasicNameValuePair("bin_client", customerInformationJSON.get("bin").toString()));
                    params.add(new BasicNameValuePair("corp_city", customerInformationJSON.get("companyRegistrationCity").toString()));
                    //params.add(new BasicNameValuePair("conn_type", technicalSpecificationsJSON.get("connectionType").toString()));
                    params.add(new BasicNameValuePair("conn_type", "2"));

                    JSONObject region = productCatalogClientJSON.getJSONObject("kcellRegion");
                    if("Almaty Region".equals(region.get("name").toString())){
                        params.add(new BasicNameValuePair("fk_region", "1"));
                    } else if("Astana Region".equals(region.get("name").toString())){
                        params.add(new BasicNameValuePair("fk_region", "8"));
                    } else if("North Region".equals(region.get("name").toString())){
                        params.add(new BasicNameValuePair("fk_region", "3"));
                    } else if("South Region".equals(region.get("name").toString())){
                        params.add(new BasicNameValuePair("fk_region", "2"));
                    } else if("West Region".equals(region.get("name").toString())){
                        params.add(new BasicNameValuePair("fk_region", "4"));
                    } else if("East Region".equals(region.get("name").toString())){
                        params.add(new BasicNameValuePair("fk_region", "7"));
                    }

                    params.add(new BasicNameValuePair("kase", customerInformationJSON.get("salesRepresentative").toString()));

                    JSONObject regionalHeadUserJSON = productCatalogClientJSON.getJSONObject("regionalHeadUser");
                    params.add(new BasicNameValuePair("region_head", regionalHeadUserJSON.get("username").toString()));

                    JSONObject servManagerUserJSON = productCatalogClientJSON.getJSONObject("servManagerUser");
                    params.add(new BasicNameValuePair("reten", servManagerUserJSON.get("username").toString()));

                    JSONObject supervisorUserJSON = productCatalogClientJSON.getJSONObject("supervisorUser");
                    params.add(new BasicNameValuePair("reten_super", supervisorUserJSON.get("username").toString()));

                    if(sipProtocolJSON.has("ipVoiceTraffic") && sipProtocolJSON.has("ipSignaling")){
                        if(sipProtocolJSON.get("ipVoiceTraffic").equals(sipProtocolJSON.get("ipSignaling"))){
                            params.add(new BasicNameValuePair("ips", sipProtocolJSON.get("ipVoiceTraffic").toString()));
                        } else {
                            params.add(new BasicNameValuePair("ips", sipProtocolJSON.get("ipVoiceTraffic").toString() + "," + sipProtocolJSON.get("ipSignaling")));
                        }
                    } else if(sipProtocolJSON.has("ipVoiceTraffic")){
                        params.add(new BasicNameValuePair("ips", sipProtocolJSON.get("ipVoiceTraffic").toString()));
                    } else if(sipProtocolJSON.has("ipSignaling")){
                        params.add(new BasicNameValuePair("ips", sipProtocolJSON.get("ipSignaling").toString()));
                    }

                    params.add(new BasicNameValuePair("channel_type", "1"));
                    if (technicalSpecificationsJSON.has("connectionPoint")) {
                        params.add(new BasicNameValuePair("voice_platform", technicalSpecificationsJSON.get("connectionPoint").toString()));
                    }
                    if (technicalSpecificationsJSON.has("pbxNumbers")) {
                        params.add(new BasicNameValuePair("clt_number", technicalSpecificationsJSON.get("pbxNumbers").toString()));
                    }

                    saoRequest.put("params", params.toString());

                    CloseableHttpClient client = HttpClients.createDefault();
                    HttpPost httpPost = new HttpPost(new URI(saoUrl+"/PbxClientUpdate"));
                    httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
                    CloseableHttpResponse response = client.execute(httpPost);
                    HttpEntity entity = response.getEntity();
                    String responseString = EntityUtils.toString(entity, "UTF-8");

                    JSONObject responseJSON = new JSONObject(responseString);

                    client.close();

                    JsonValue saoRequestJsonValue = SpinValues.jsonValue(saoRequest.toString()).create();
                    delegateExecution.setVariable("saoRequest", saoRequestJsonValue);

                    JsonValue saoResponseJsonValue = SpinValues.jsonValue(responseJSON.toString()).create();
                    delegateExecution.setVariable("saoResponse", saoResponseJsonValue);
                }
            }
        }
    }
}
