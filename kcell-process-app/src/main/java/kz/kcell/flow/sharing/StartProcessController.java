package kz.kcell.flow.sharing;

import io.minio.errors.*;
import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.plugin.variable.SpinValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xmlpull.v1.XmlPullParserException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Log
// baseUri + /camunda/startProcess/sharing
@RequestMapping("/startProcess")
public class StartProcessController {

    @Autowired
    TaskService taskService;

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    HistoryService historyService;

    private final String baseUri;

    @Autowired
    public StartProcessController(@Value("${mail.message.baseurl:http://localhost}") String baseUri) {
        this.baseUri = baseUri;
    }

    @RequestMapping(value = "/sharing", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> startSharing(HttpServletRequest request) throws NoSuchAlgorithmException, IOException,KeyStoreException, KeyManagementException, ParseException {


        HttpGet httpGet = new HttpGet(baseUri + "/directory-management/networkinfrastructure/plan/findCurrentToStartPlanSites");
        httpGet.addHeader("Referer", baseUri);

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");

        JSONArray plans = new JSONArray(responseString);

        for (int i=0; i < plans.length(); i++) {
            JSONObject plan = (JSONObject) plans.get(i);
            JSONObject params = plan.getJSONObject("params");
            String host = params.get("host").toString();

            if ( host.equals("Kcell") ) {
                String businessKey = params.get("site_id").toString() + '_' + params.get("site_name").toString();
                List<Execution> executions = runtimeService.createExecutionQuery().processDefinitionKey("SiteSharingTopProcess").processInstanceBusinessKey(businessKey).list();

                if (executions.isEmpty()) {
                    String positionNumber = plan.get("position_number").toString();
                    String sharingPlanStatus = plan.get("status").toString();
                    String site = params.get("infrastructure_owner").toString();
                    String isSpecialSite = params.get("isSpecialSite").toString();

                    // Определение региона по site_id:
                    String siteRegion = "";
                    if ( params.get("site_id").toString().startsWith("0")) {
                        siteRegion = "Almaty";
                    } else if ( params.get("site_id").toString().startsWith("1") || params.get("site_id").toString().startsWith("2") ) {
                        if (params.get("site_id").toString().startsWith("11")) {
                            siteRegion ="Astana";
                        } else {
                            siteRegion = "North";
                        }
                    }   else if ( params.get("site_id").toString().startsWith("3")) {
                        siteRegion = "East";
                    }   else if ( params.get("site_id").toString().startsWith("4")) {
                        siteRegion = "South";
                    }   else {  siteRegion ="West"; }

                    log.info("Process starting");
                    Map<String, Object> variables = new HashMap<String,Object>();
                    variables.put("host", host);
                    variables.put("site", site);
                    variables.put("sharingType", "sharing");
                    variables.put("sharingPlan", SpinValues.jsonValue(params.toString()));
                    variables.put("isSpecialSite", isSpecialSite);
                    variables.put("positionNumber", positionNumber);
                    variables.put("siteRegion", siteRegion);
                    variables.put("sharingPlanStatus", sharingPlanStatus);

                    ProcessInstance instance = runtimeService.startProcessInstanceByKey("SiteSharingTopProcess", businessKey, variables);
                    log.info("instance:");
                    log.info(instance.getBusinessKey());
                } else {
                    log.info("Process instance with Business Key " + businessKey + " exists in History");
                }
            }
        }

        return ResponseEntity.ok("Success");
    }

    @RequestMapping(value = "/completed-sharing", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> startAndFinishSharing(HttpServletRequest request) throws NoSuchAlgorithmException, IOException,KeyStoreException, KeyManagementException, ParseException {


        HttpGet httpGet = new HttpGet(baseUri + "/directory-management/networkinfrastructure/plan/findCurrentToStartAndFinishPlanSites");
        httpGet.addHeader("Referer", baseUri);

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");

        JSONArray plans = new JSONArray(responseString);

        for (int i=0; i < plans.length(); i++) {
            JSONObject plan = (JSONObject) plans.get(i);

            log.info("StartProcessController position_number: " + plan.get("position_number").toString() + ", acceptance_date: " + plan.get("acceptance_date").toString() + ", status: " + plan.get("status").toString());

            if(plan.has("acceptance_date") && !plan.get("acceptance_date").equals(null)) {
                JSONObject params = plan.getJSONObject("params");
                String host = params.get("host").toString();
                // Perhaps Rollout should be added to Business Key to be able to add next time(next roll out)
                String businessKey = params.get("site_id").toString() + '_' + params.get("site_name").toString();
                List<HistoricProcessInstance> processInstances = historyService.createHistoricProcessInstanceQuery().finished().processDefinitionKey("SiteSharingTopProcess").processInstanceBusinessKey(businessKey).list();

                if (processInstances.isEmpty()) {
                    String positionNumber = plan.get("position_number").toString();
                    String acceptanceDateString = plan.get("acceptance_date").toString();
                    Boolean startAndFinish = (Boolean) plan.get("start_and_finish");

                    log.info("StartProcessController startAndFinish: " + startAndFinish);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    Date acceptanceDate = sdf.parse(acceptanceDateString);
                    log.info("StartProcessControlleracceptanceDate: " + acceptanceDate);


                    String sharingPlanStatus = plan.get("status").toString();
                    String site = params.get("infrastructure_owner").toString();
                    String isSpecialSite = params.get("isSpecialSite").toString();

                    // Определение региона по site_id:
                    String siteRegion = "";
                    if (params.get("site_id").toString().startsWith("0")) {
                        siteRegion = "Almaty";
                    } else if (params.get("site_id").toString().startsWith("1") || params.get("site_id").toString().startsWith("2")) {
                        if (params.get("site_id").toString().startsWith("11")) {
                            siteRegion = "Astana";
                        } else {
                            siteRegion = "North";
                        }
                    } else if (params.get("site_id").toString().startsWith("3")) {
                        siteRegion = "East";
                    } else if (params.get("site_id").toString().startsWith("4")) {
                        siteRegion = "South";
                    } else {
                        siteRegion = "West";
                    }

                    //if (host.equals("Kcell")) {
                    log.info("Process starting");
                    Map<String, Object> variables = new HashMap<String, Object>();
                    variables.put("host", host);
                    variables.put("site", site);
                    variables.put("sharingType", "sharing");
                    variables.put("sharingPlan", SpinValues.jsonValue(params.toString()));
                    variables.put("isSpecialSite", isSpecialSite);
                    variables.put("positionNumber", positionNumber);
                    variables.put("siteRegion", siteRegion);
                    variables.put("sharingPlanStatus", sharingPlanStatus);

                    variables.put("replanStatus", "notreplan");
                    variables.put("acceptanceDate", acceptanceDate);
                    variables.put("startAndFinish", startAndFinish);

                    ProcessInstance instance = runtimeService.startProcessInstanceByKey("SiteSharingTopProcess", businessKey, variables);
                    log.info("instance:");
                    log.info("Business Key: " + instance.getBusinessKey());
                    //}
                } else {
                    log.info("Process instance with Business Key " + businessKey + " exists in History");
                }
            }
        }
        return ResponseEntity.ok("Success");
    }
}
