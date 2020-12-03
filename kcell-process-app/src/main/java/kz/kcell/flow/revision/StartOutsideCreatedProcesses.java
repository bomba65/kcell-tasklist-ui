package kz.kcell.flow.revision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.kcell.bpm.SetPricesDelegate;
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
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceModificationBuilder;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.json.JSONArray;
import org.json.JSONObject;
import org.msgpack.util.json.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/startprocesses")
@Log
public class StartOutsideCreatedProcesses {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RuntimeService runtimeService;

    @Value("${mail.message.baseurl:http://localhost}")
    String baseUri;

    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> delete() throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        ObjectMapper mapper = new ObjectMapper();
        InputStream fis = SetPricesDelegate.class.getResourceAsStream("/revision/new_process.json");

        InputStreamReader reader = new InputStreamReader(fis, "utf-8");
        ArrayNode json = (ArrayNode) mapper.readTree(reader);

        for (JsonNode data : json) {
            String jrno = data.get("jrno").asText();

            List<ProcessInstance> plist = runtimeService.createProcessInstanceQuery().processDefinitionKey("Revision").processInstanceBusinessKey(jrno).list();
            for(ProcessInstance p: plist){
                runtimeService.deleteProcessInstance(p.getRootProcessInstanceId(), "unused processes", true);
            }
        }

        return ResponseEntity.ok("ok");
    }

    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> execute() throws Exception {

        StringBuilder notFoundSitenames = new StringBuilder("");

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        ObjectMapper mapper = new ObjectMapper();
        InputStream fis = SetPricesDelegate.class.getResourceAsStream("/revision/new_process.json");

        InputStreamReader reader = new InputStreamReader(fis, "utf-8");
        ArrayNode json = (ArrayNode) mapper.readTree(reader);

        for (JsonNode data : json) {
            String jrno = data.get("jrno").asText();

            List<String> siteNames = new ArrayList<>();

            if(data.get("sitename").asText().contains(";")){
                StringTokenizer bySemicolon = new StringTokenizer(data.get("sitename").asText(), ";");
                while(bySemicolon.hasMoreTokens()){
                    String sitename = bySemicolon.nextToken().trim();
                    siteNames.add(sitename);
                }
            } else {
                siteNames.add(data.get("sitename").asText());
            }

            for (String sitename: siteNames){
                HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/sites/search/findByNameIgnoreCaseContaining?name=" + sitename.replaceAll("\\D+",""));
                httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
                httpGet.addHeader("Referer", baseUri);
                HttpResponse response = httpclient.execute(httpGet);

                if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300){
                    throw new RuntimeException("Site search by name " + sitename + " returns code " + response.getStatusLine().getStatusCode());
                }

                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity);

                JSONObject siteDetail = new JSONObject(content);
                EntityUtils.consume(response.getEntity());

                if(siteDetail.getJSONObject("_embedded").getJSONArray("sites").length() == 0){
                    notFoundSitenames.append(jrno + ":" + sitename + "; ");
                }
            }
        }

        return ResponseEntity.ok(notFoundSitenames.toString());
    }

    @RequestMapping(value = "/execute/{processDefId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> execute(@PathVariable("processDefId") String processDefId) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
            builder.build());
        CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
            sslsf).build();

        HttpGet httpCatalogsGet = new HttpGet(baseUri + "/api/catalogs?v=12");
        HttpResponse catalogsResponse = httpclient.execute(httpCatalogsGet);

        if(catalogsResponse.getStatusLine().getStatusCode() < 200 || catalogsResponse.getStatusLine().getStatusCode() >= 300){
            throw new RuntimeException("Get catalogs returns code " + catalogsResponse.getStatusLine().getStatusCode());
        }

        HttpEntity catalogsEntity = catalogsResponse.getEntity();
        String catalogsContent = EntityUtils.toString(catalogsEntity);

        JSONObject catalogs = new JSONObject(catalogsContent);
        EntityUtils.consume(catalogsResponse.getEntity());

        ObjectMapper mapper = new ObjectMapper();
        InputStream fis = SetPricesDelegate.class.getResourceAsStream("/revision/new_process.json");

        InputStreamReader reader = new InputStreamReader(fis, "utf-8");
        ArrayNode json = (ArrayNode) mapper.readTree(reader);

        List<String> opticalWorks = new ArrayList<>();
        opticalWorks.add("9.90");
        opticalWorks.add("9.91");
        opticalWorks.add("9.92");
        opticalWorks.add("9.93");
        opticalWorks.add("9.94");
        opticalWorks.add("9.95");
        opticalWorks.add("9.96");
        opticalWorks.add("9.97");
        opticalWorks.add("9.98");
        opticalWorks.add("9.99");
        opticalWorks.add("9.100");
        opticalWorks.add("9.101");
        opticalWorks.add("9.102");
        opticalWorks.add("9.103");
        opticalWorks.add("9.104");
        opticalWorks.add("9.105");
        opticalWorks.add("9.106");

        List<String> sendToHeadWorks = new ArrayList<>();
        sendToHeadWorks.add("2.4");
        sendToHeadWorks.add("9.9");
        sendToHeadWorks.add("9.10");
        sendToHeadWorks.add("9.11");
        sendToHeadWorks.add("9.20");
        sendToHeadWorks.add("9.21");
        sendToHeadWorks.add("9.22");
        sendToHeadWorks.add("9.23");
        sendToHeadWorks.add("9.24");
        sendToHeadWorks.add("9.25");
        sendToHeadWorks.add("9.26");
        sendToHeadWorks.add("9.27");
        sendToHeadWorks.add("9.28");

        List<String> subContractorsMaterials = new ArrayList<>();
        subContractorsMaterials.add("6.1");
        subContractorsMaterials.add("6.2");
        subContractorsMaterials.add("6.3");
        subContractorsMaterials.add("6.4");
        subContractorsMaterials.add("6.5");
        subContractorsMaterials.add("6.8");
        subContractorsMaterials.add("6.9");
        subContractorsMaterials.add("6.10");
        subContractorsMaterials.add("6.12");
        subContractorsMaterials.add("6.13");
        subContractorsMaterials.add("6.14");
        subContractorsMaterials.add("6.15");
        subContractorsMaterials.add("6.16");
        subContractorsMaterials.add("6.17");
        subContractorsMaterials.add("6.18");
        subContractorsMaterials.add("6.19");
        subContractorsMaterials.add("6.21");
        subContractorsMaterials.add("6.22");
        subContractorsMaterials.add("6.23");
        subContractorsMaterials.add("6.24");
        subContractorsMaterials.add("6.25");
        subContractorsMaterials.add("6.27");
        subContractorsMaterials.add("6.28");
        subContractorsMaterials.add("6.29");
        subContractorsMaterials.add("7.1");
        subContractorsMaterials.add("7.2");
        subContractorsMaterials.add("7.3");
        subContractorsMaterials.add("7.5");
        subContractorsMaterials.add("7.6");
        subContractorsMaterials.add("7.7");
        subContractorsMaterials.add("7.8");
        subContractorsMaterials.add("7.9");
        subContractorsMaterials.add("7.10");
        subContractorsMaterials.add("7.11");
        subContractorsMaterials.add("8.7");
        subContractorsMaterials.add("9.58");

        List<String> kcellMaterials = Arrays.asList(new String[]{
            "1.1", "1.2", "1.3", "1.4", "2.1", "2.2", "2.3", "2.4", "2.5", "2.6", "2.7", "2.8", "2.9", "2.10", "2.11", "2.12", "2.13", "2.14", "2.15", "2.16", "2.17", "2.18", "2.19", "2.20", "2.21", "2.22", "2.23", "2.24", "2.26", "2.28", "2.29", "2.30", "2.31", "2.32", "2.33", "2.34", "2.35", "2.36", "2.37", "2.38", "2.39", "2.40", "2.41", "2.42", "2.43", "2.44", "2.45", "2.46",
            "2.47", "2.48", "2.49", "2.50", "2.51", "2.52", "2.53", "2.54", "2.63", "3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "3.9", "3.10", "3.11", "3.12", "3.13", "3.14", "3.15", "3.16", "3.17", "3.18", "3.19", "3.20", "3.21", "3.22", "3.23", "3.24", "3.25", "3.26", "3.27", "3.28", "3.29", "3.30", "3.31", "3.40", "3.41", "3.49", "3.54", "4.14", "4.15", "5.1",
            "5.2", "5.3", "5.4", "5.9", "5.13", "6.7", "6.11", "6.20", "6.26", "7.4", "8.8", "8.13", "8.14", "8.15", "8.16", "8.20", "8.21", "8.22", "8.23", "8.24", "8.25", "9.3", "9.4", "9.5", "9.6", "9.7", "9.8", "9.9", "9.10", "9.11", "9.12", "9.13", "9.14", "9.15", "9.16", "9.17", "9.18", "9.19", "9.20", "9.21", "9.22", "9.23", "9.24", "9.25", "9.26", "9.27", "9.28", "9.29",
            "9.30", "9.31", "9.32", "9.33", "9.34", "9.35", "9.36", "9.37", "9.38", "9.39", "9.40", "9.41", "9.42", "9.43", "9.44", "9.45", "9.46", "9.47", "9.48", "9.49", "9.50", "9.51", "9.52", "9.53", "9.54", "9.55", "9.56", "9.61", "9.63", "9.65", "9.66", "9.67", "9.68", "9.69", "9.70", "9.71", "9.72", "9.73", "9.74", "9.77", "9.78", "9.79", "9.81", "9.82", "9.83", "9.84",
            "9.85", "9.86", "9.87", "9.88", "9.89", "9.90", "9.91", "9.92", "9.93", "9.94", "9.95", "9.96", "9.97", "9.98", "9.99", "9.100", "9.101", "9.102", "9.103", "9.104", "9.105", "9.106", "9.107", "10.1", "10.2"
        });

        List<String> allMaterials = Arrays.asList(new String[]{
             "2.25", "2.27", "2.55", "2.56", "2.57", "2.58", "2.59", "2.60", "2.61", "2.62", "3.32", "3.33", "3.34", "3.35", "3.36", "3.37", "3.38", "3.39", "3.42", "3.43", "3.44", "3.45", "3.46", "3.47", "3.48", "3.50", "3.51", "3.52", "3.53", "4.1", "4.2", "4.3", "4.4", "4.5", "4.6", "4.7", "4.8", "4.9", "4.10", "4.11", "4.12", "4.13", "4.16", "4.17", "4.18", "4.19", "5.5", "5.6",
            "5.7", "5.8", "5.10", "5.11", "5.12", "5.14", "5.15", "5.16", "5.17", "6.6", "8.1", "8.2", "8.3", "8.4", "8.5", "8.6", "8.9", "8.10", "8.11", "8.12", "8.17", "8.18", "8.19", "9.1", "9.2", "9.57", "9.59", "9.60", "9.62", "9.64", "9.75", "9.76", "9.80"
        });

        Map<String, String> regions = new HashMap<>();
        regions.put("Almaty", "alm");
        regions.put("West", "west");
        regions.put("South", "south");
        regions.put("East", "east");
        regions.put("North & Central", "nc");
        regions.put("Astana", "astana");

        Map<String, Integer> reasons = new HashMap<>();
        reasons.put("P&O", 1);
        reasons.put("SAO", 4);
        reasons.put("TNU", 2);
        reasons.put("S&FM", 3);

        List<String> startedProcessInstances = new ArrayList<>();

        int limit = 1;
        int t = 0;
        for (JsonNode data : json) {
            if(t<limit){
                String jrno = data.get("jrno").asText();

                long count = runtimeService.createProcessInstanceQuery().processDefinitionKey("Revision").processInstanceBusinessKey(jrno).count();

                if(count == 0L){
                    Map<String, Object> vars = new HashMap<>();
                    vars.put("contract", 1);
                    vars.put("contractor", 4);
                    vars.put("createJRResult", "approved");
                    vars.put("explanation", data.get("comments").asText());

                    String requestedBy = data.get("requestedBy").asText().trim();
                    String firstName = requestedBy.substring(0, requestedBy.indexOf(".")).toLowerCase();
                    firstName = firstName.substring(0,1).toUpperCase() + firstName.substring(1);
                    String lastName = requestedBy.substring(requestedBy.indexOf(".")+1).toLowerCase();
                    lastName = lastName.substring(0,1).toUpperCase() + lastName.substring(1);

                    ObjectNode initiatorFull = mapper.createObjectNode();
                    initiatorFull.put("id", firstName + "." + lastName + "@kcell.kz");
                    initiatorFull.put("email", firstName + "." + lastName + "@kcell.kz");
                    initiatorFull.put("firstName", firstName);
                    initiatorFull.put("lastName", lastName);
                    // {"id":"demo","firstName":"Demo","lastName":"Demo","email":"demo@camunda.org"}

                    vars.put("initiatorFull", SpinJsonNode.JSON(initiatorFull.toString()));
                    vars.put("isNewProcessCreated", "false");

                    String jobDescription = data.get("jobDescription").asText();
                    Boolean opticalWork = false;
                    for(String opw: opticalWorks){
                        if(jobDescription.contains(opw+" ")){
                            opticalWork = true;
                        }
                    }
                    vars.put("isOptical", opticalWork);
                    vars.put("isPermitDocsNeeded", "false");
                    vars.put("jrNumber", jrno);
                    vars.put("leasingRequired", "No");
                    vars.put("leasingRequiredCheckbox", false);
                    vars.put("mainContract", "Roll-outRevision2020");
                    vars.put("materialsRequired", "No");
                    vars.put("materialsRequiredCheckbox", false);
                    vars.put("powerRequiredCheckbox", false);
                    vars.put("powerRequired", "No");
                    vars.put("prCreationInProgress", false);
                    vars.put("priority", "regular");
                    vars.put("project", "N/A");
                    vars.put("ptype", "prod");
                    vars.put("ptypeCheckbox", false);
                    vars.put("sendNotifyToRegionHeads", false);
                    vars.put("worksBelongsTo", "No");

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.HOUR, 6);

                    vars.put("requestedDate", calendar.getTime());
                    vars.put("siteRegion", regions.get(data.get("region").asText()));
                    vars.put("reason", reasons.get(data.get("jrReason").asText()));
                    // vars.put("sendNotifyToRegionHeads", sendToHeadWorks.)

                    vars.put("relatedTo", "Field works");

                    SimpleDateFormat fff = new SimpleDateFormat("MM/dd/yy");
                    Calendar requestedDate = Calendar.getInstance();
                    requestedDate.setTime(fff.parse(data.get("requestedDate").asText()));
                    requestedDate.add(Calendar.HOUR, 6);

                    ArrayNode resolutions = (ArrayNode) mapper.createArrayNode();
                    ObjectNode res = mapper.createObjectNode();
                    res.put("processInstanceId", "noProcessInstanceId");
                    res.put("assignee", firstName + "." + lastName + "@kcell.kz");
                    res.put("assigneeName", firstName + " " + lastName);
                    res.put("resolution", "created");
                    res.put("comment", "Процесс был создан для работы с заявками из системы RN-RS. " + data.get("comments").asText());
                    res.put( "taskId", "noTaskId");
                    res.put( "taskName", "Job Request Start");
                    res.put( "taskEndDate", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXX").format(requestedDate.getTime()));
                    res.put( "visibility", "all");
                    resolutions.add(res);
                    vars.put("resolutions", SpinJsonNode.JSON(resolutions.toString()));

                    vars.put("starter", firstName + "." + lastName + "@kcell.kz");

                    vars.put("contractorJobAssignedDate", requestedDate.getTime());
                    vars.put("isNewProcessCreated", false);
                    vars.put("isPermitDocsNeeded", false);

                    ArrayNode jobWorks = mapper.createArrayNode();

                    ArrayNode relatedSites = mapper.createArrayNode();

                    List<String> siteNames = new ArrayList<>();

                    if(data.get("sitename").asText().contains(";")){
                        StringTokenizer bySemicolon = new StringTokenizer(data.get("sitename").asText(), ";");
                        while(bySemicolon.hasMoreTokens()){
                            String sitename = bySemicolon.nextToken().trim();
                            siteNames.add(sitename);
                        }
                    } else {
                        siteNames.add(data.get("sitename").asText());
                    }

                    boolean siteDataPut = false;
                    for (String sitename: siteNames){
                        HttpGet httpGet = new HttpGet(baseUri + "/asset-management/api/sites/search/findByNameIgnoreCaseContaining?name=" + sitename.replaceAll("\\D+",""));
                        httpGet.addHeader("Content-Type", "application/json;charset=UTF-8");
                        httpGet.addHeader("Referer", baseUri);
                        HttpResponse response = httpclient.execute(httpGet);

                        if(response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300){
                            throw new RuntimeException("Site search by name " + sitename + " returns code " + response.getStatusLine().getStatusCode());
                        }

                        HttpEntity entity = response.getEntity();
                        String content = EntityUtils.toString(entity);

                        JSONObject siteDetail = new JSONObject(content);
                        EntityUtils.consume(response.getEntity());

                        if(siteDetail.getJSONObject("_embedded").getJSONArray("sites").length() > 0){
                            ObjectNode relatedSite = mapper.createObjectNode();
                            relatedSite.put("name", siteDetail.getJSONObject("_embedded").getJSONArray("sites").getJSONObject(0).getString("name"));
                            relatedSite.put("site_name", sitename);
                            relatedSite.put("id", siteDetail.getJSONObject("_embedded").getJSONArray("sites").getJSONObject(0).getJSONObject("_links").getJSONObject("self").getString("href").replace(baseUri + "/asset-management/api/sites/",""));
                            relatedSite.set("params", mapper.readTree(siteDetail.getJSONObject("_embedded").getJSONArray("sites").getJSONObject(0).getJSONObject("params").toString()));
                            relatedSites.add(relatedSite);

                            if(!siteDataPut){
                                vars.put("site", Integer.valueOf(siteDetail.getJSONObject("_embedded").getJSONArray("sites").getJSONObject(0).getJSONObject("_links").getJSONObject("self").getString("href").replace(baseUri + "/asset-management/api/sites/","")));
                                vars.put("site_name", sitename);
                                vars.put("siteName", siteDetail.getJSONObject("_embedded").getJSONArray("sites").getJSONObject(0).getString("name"));
                                vars.put("siteStatus", "working site");
                                siteDataPut = true;
                            }
                        }
                    }

                    List<String> sapServiceNumbers = new ArrayList<>();
                    List<String> numbers = new ArrayList<>();

                    Pattern p = Pattern.compile("\\d+\\.\\d+");
                    Matcher m = p.matcher(jobDescription);
                    while (m.find()) {
                        sapServiceNumbers.add(m.group());
                    }

                    Pattern p1 = Pattern.compile("- \\d+");
                    Matcher m1 = p1.matcher(jobDescription);
                    while (m1.find()) {
                        numbers.add(m1.group());
                    }

                    JSONArray works = catalogs.getJSONArray("works");

                    int it = 0;
                    for(String job: sapServiceNumbers){
                        ObjectNode jobWork = mapper.createObjectNode();
                        jobWork.put("sapServiceNumber", job);
                        jobWork.set("relatedSites", relatedSites);

                        for(int j=0; j<works.length(); j++){
                            if(job.equals(works.getJSONObject(j).getString("sapServiceNumber"))){
                                jobWork.put("displayServiceName", works.getJSONObject(j).getString("displayServiceName"));
                                jobWork.put("materialUnit", works.getJSONObject(j).getString("units"));
                                jobWork.put("quantity", Integer.valueOf(numbers.get(it).replace("- ", "")));
                                jobWork.put("materialsProvidedByDisabled", kcellMaterials.contains(job) || subContractorsMaterials.contains(job));
                                jobWork.put("materialsProvidedBy", subContractorsMaterials.contains(job) ? "subcontractor" : "kcell");
                            }
                        }
                        jobWorks.add(jobWork);
                        it++;
                    }
                    vars.put("jobWorks", SpinJsonNode.JSON(jobWorks.toString()));
                    vars.put("createdAutomatically", true);
                    vars.put("regionGroupHeadApprovalTaskResult", "approved");
                    vars.put("validityDate", calendar.getTime());

                    for (Map.Entry<String,Object> entry: vars.entrySet()){
                        System.out.println("key = " + entry.getKey() + ", value = " + entry.getValue());
                    }

                    ProcessInstance instance = runtimeService.startProcessInstanceById(processDefId, jrno, vars);
                    startedProcessInstances.add(instance.getRootProcessInstanceId());
                }

                for(String pid: startedProcessInstances){
                    ProcessInstanceModificationBuilder builder1 = runtimeService.createProcessInstanceModification(pid);
                    builder1.startBeforeActivity("IntermediateThrowEvent_open_not_performed1");
                    builder1.cancelAllForActivity("SubProcess_0cd7y34");
                    builder1.executeAsync();
                }

                t++;
            }
        }

        httpclient.close();
        return ResponseEntity.ok("ok");
    }
}
