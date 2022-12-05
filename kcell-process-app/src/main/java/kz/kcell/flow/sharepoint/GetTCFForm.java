package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Arrays;
import java.util.Base64;


@Service("getTCFForm")
@Log
public class GetTCFForm implements JavaDelegate {

    @Autowired
    private Environment environment;

    @Value("${sharepoint.forms.requestBody:SP.Data.TCF_x005f_testListItem}")
    private String sharepointRequestBody;

    private final String baseUri;
    private final String username;
    private final String pwd;
    private final String productCatalogUrl;
    private final String productCatalogAuth;
    private final String sharepointUrlPart;

    @Autowired
    public GetTCFForm(@Value("${sharepoint.forms.url:https://sp19.kcell.kz/forms/_api}") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd,
                      @Value("${product.catalog.url:http://ldb-al-preprod.kcell.kz}") String productCatalogUrl, @Value("${product.catalog.auth:app.camunda.user:Asd123Qwerty!}") String productCatalogAuth,
                      @Value("${sharepoint.forms.url.part:TCF_test}") String sharepointUrlPart) {
        this.baseUri = baseUri;
        this.username = username;
        this.pwd = pwd;
        this.productCatalogUrl = productCatalogUrl;
        this.productCatalogAuth = productCatalogAuth;
        this.sharepointUrlPart = sharepointUrlPart;
    }

    private String getVasChargingMtsBilling(String shortNumber) {
        try {
            String encoding = Base64.getEncoder().encodeToString((this.productCatalogAuth).getBytes("UTF-8"));
            HttpClient httpclient = HttpClients.custom().build();
            log.info("getVasChargingMtsBilling REQUEST [" + Thread.currentThread().getName() + "]:");
            log.info(this.productCatalogUrl + "/vas_charging_mts/short_number/" + URLEncoder.encode(shortNumber, "UTF-8"));
            HttpGet httpGet = new HttpGet(this.productCatalogUrl + "/vas_charging_mts/short_number/" + URLEncoder.encode(shortNumber, "UTF-8"));
            httpGet.setHeader("Authorization", "Basic " + encoding);
            HttpResponse httpResponse = httpclient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String content = EntityUtils.toString(entity);
            log.info("getVasChargingMtsBilling RESULT [" + Thread.currentThread().getName() + "]: " + content);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                EntityUtils.consume(httpResponse.getEntity());
                return content;
            } else {
                return "[]";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String postVasChargingMts(String requestBody) {
        try {
            String encoding = Base64.getEncoder().encodeToString((this.productCatalogAuth).getBytes("UTF-8"));
            StringEntity shortNumberData = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            log.info("postVasChargingMts REQUEST [" + Thread.currentThread().getName() + "]: " + this.productCatalogUrl + "/vas_charging_mts");
            log.info("postVasChargingMts BODY [" + Thread.currentThread().getName() + "]:" + requestBody);
            HttpPost shortNumberPost = new HttpPost(new URI(this.productCatalogUrl + "/vas_charging_mts"));
            shortNumberPost.setHeader("Authorization", "Basic " + encoding);
            shortNumberPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            shortNumberPost.setEntity(shortNumberData);
            HttpClient shortNumberHttpClient = HttpClients.createDefault();
            HttpResponse shortNumberResponse = shortNumberHttpClient.execute(shortNumberPost);
            HttpEntity entity = shortNumberResponse.getEntity();
            String content = EntityUtils.toString(entity);
            log.info("postVasChargingMts RESPONSE [" + Thread.currentThread().getName() + "]:" + content);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String putVasChargingMts(String vasId, String requestBody) {
        try {
            String encoding = Base64.getEncoder().encodeToString((this.productCatalogAuth).getBytes("UTF-8"));
            StringEntity shortNumberData = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            log.info("putVasChargingMts REQUEST [" + Thread.currentThread().getName() + "]: " + this.productCatalogUrl + "/vas_charging_mts/" + vasId);
            log.info("putVasChargingMts BODY [" + Thread.currentThread().getName() + "]:" + requestBody);
            HttpPut shortNumberPut = new HttpPut(new URI(this.productCatalogUrl + "/vas_charging_mts/" + vasId));
            shortNumberPut.setHeader("Authorization", "Basic " + encoding);
            shortNumberPut.addHeader("Content-Type", "application/json;charset=UTF-8");
            shortNumberPut.setEntity(shortNumberData);
            HttpClient shortNumberHttpClient = HttpClients.createDefault();
            HttpResponse shortNumberResponse = shortNumberHttpClient.execute(shortNumberPut);
            HttpEntity entity = shortNumberResponse.getEntity();
            String content = EntityUtils.toString(entity);
            log.info("putVasChargingMts RESPONSE [" + Thread.currentThread().getName() + "]:" + content);
            if (shortNumberResponse.getStatusLine().getStatusCode() == 200) {
                return content;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getShortNumberId(String shortNumber, String serviceTypeId) {
        try {
            String encoding = Base64.getEncoder().encodeToString((this.productCatalogAuth).getBytes("UTF-8"));
            CloseableHttpClient httpclient = HttpClients.custom().build();
            log.info("getShortNumberId REQUEST [" + Thread.currentThread().getName() + "]:");
            log.info(this.productCatalogUrl + "/vas_short_numbers/short_number/" + URLEncoder.encode(shortNumber, "UTF-8") + "/service_type_id/" + serviceTypeId);
            HttpGet httpGet = new HttpGet(this.productCatalogUrl + "/vas_short_numbers/short_number/" + URLEncoder.encode(shortNumber, "UTF-8") + "/service_type_id/" + serviceTypeId);
            httpGet.setHeader("Authorization", "Basic " + encoding);
            HttpResponse httpResponse = httpclient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String content = EntityUtils.toString(entity);
            log.info("getShortNumberId RESULT [" + Thread.currentThread().getName() + "]: " + content);
            EntityUtils.consume(httpResponse.getEntity());
            httpclient.close();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String postVasUrls(String requestBody) {
        try {
            String encoding = Base64.getEncoder().encodeToString((this.productCatalogAuth).getBytes("UTF-8"));
            StringEntity shortNumberData = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            log.info("postVasUrls REQUEST [" + Thread.currentThread().getName() + "]: " + this.productCatalogUrl + "/vas_urls");
            log.info("postVasUrls BODY [" + Thread.currentThread().getName() + "]:" + requestBody);
            HttpPost shortNumberPost = new HttpPost(new URI(this.productCatalogUrl + "/vas_urls"));
            shortNumberPost.setHeader("Authorization", "Basic " + encoding);
            shortNumberPost.addHeader("Content-Type", "application/json;charset=UTF-8");
            shortNumberPost.setEntity(shortNumberData);
            HttpClient shortNumberHttpClient = HttpClients.createDefault();
            HttpResponse shortNumberResponse = shortNumberHttpClient.execute(shortNumberPost);
            HttpEntity entity = shortNumberResponse.getEntity();
            String content = EntityUtils.toString(entity);
            log.info("postVasUrls RESPONSE [" + Thread.currentThread().getName() + "]:" + content);
            return content;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String putVasUrls(String vasId, String requestBody) {
        try {
            String encoding = Base64.getEncoder().encodeToString((this.productCatalogAuth).getBytes("UTF-8"));
            StringEntity shortNumberData = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            log.info("putVasUrls REQUEST [" + Thread.currentThread().getName() + "]: " + this.productCatalogUrl + "/vas_urls/" + vasId);
            log.info("putVasUrls BODY [" + Thread.currentThread().getName() + "]:" + requestBody);
            HttpPut shortNumberPut = new HttpPut(new URI(this.productCatalogUrl + "/vas_urls/" + vasId));
            shortNumberPut.setHeader("Authorization", "Basic " + encoding);
            shortNumberPut.addHeader("Content-Type", "application/json;charset=UTF-8");
            shortNumberPut.setEntity(shortNumberData);
            HttpClient shortNumberHttpClient = HttpClients.createDefault();
            HttpResponse shortNumberResponse = shortNumberHttpClient.execute(shortNumberPut);
            HttpEntity entity = shortNumberResponse.getEntity();
            String content = EntityUtils.toString(entity);
            log.info("putVasUrls RESPONSE [" + Thread.currentThread().getName() + "]:" + content);
            if (shortNumberResponse.getStatusLine().getStatusCode() == 200) {
                return content;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getBilling(String shortNumber) {
        try {
            String encoding = Base64.getEncoder().encodeToString((this.productCatalogAuth).getBytes("UTF-8"));
            HttpClient httpclient = HttpClients.custom().build();
            log.info("getBilling REQUEST [" + Thread.currentThread().getName() + "]:");
            log.info(this.productCatalogUrl + "/vas_urls/short_number/" + URLEncoder.encode(shortNumber, "UTF-8"));
            HttpGet httpGet = new HttpGet(this.productCatalogUrl + "/vas_urls/short_number/" + URLEncoder.encode(shortNumber, "UTF-8"));
            httpGet.setHeader("Authorization", "Basic " + encoding);
            HttpResponse httpResponse = httpclient.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            String content = EntityUtils.toString(entity);
            log.info("getBilling RESULT [" + Thread.currentThread().getName() + "]: " + content);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                EntityUtils.consume(httpResponse.getEntity());
                return content;
            } else {
                return "[]";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
        String billingTCF = String.valueOf(delegateExecution.getVariableLocal("billingTCF"));
        JSONArray identifierJSONArray = new JSONArray(String.valueOf(delegateExecution.getVariable("identifiers")));
        JSONObject identifierJSON = identifierJSONArray.getJSONObject(0);
        String title = String.valueOf(identifierJSON.get("title"));
        String serviceTypeId = "";
        String tcfFormId = "";
        String identifierServiceName_amdocs_incoming = "";
        String identifierServiceName_amdocs_outgoing = "";
        String identifierServiceName_orga_incoming = "";
        String identifierServiceName_orga_outgoing = "";
        String[] rejectStatusByTCF = {"Canceled", "Rejected by ICTD-SNS-BCOU-BCST Supervisor", "Rejected by ICTD Specialists"};
        String processDefinitionKey = delegateExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateExecution.getProcessDefinitionId()).getKey();
        log.info("ProcessDefinitionKey=" + processDefinitionKey);
        if (isSftp) {
            if ("bulksmsConnectionKAE".equals(processDefinitionKey)) {
                if ("amdocs".equals(billingTCF)) {
                    tcfFormId = delegateExecution.getVariable("amdocsTcfFormId").toString();
                    identifierServiceName_amdocs_incoming = delegateExecution.getVariable("identifierServiceName_amdocs_incoming").toString();
                    identifierServiceName_amdocs_outgoing = delegateExecution.getVariable("identifierServiceName_amdocs_outgoing").toString();
                }
                if ("orga".equals(billingTCF)) {
                    tcfFormId = delegateExecution.getVariable("orgaTcfFormId").toString();
                    identifierServiceName_orga_incoming = delegateExecution.getVariable("identifierServiceName_orga_incoming").toString();
                    identifierServiceName_orga_outgoing = delegateExecution.getVariable("identifierServiceName_orga_outgoing").toString();
                }
                serviceTypeId = "13";
            }
            if ("freephone".equals(processDefinitionKey)) {
                if ("amdocs".equals(billingTCF)) {
                    tcfFormId = delegateExecution.getVariable("amdocsTcfFormId").toString();
                    identifierServiceName_amdocs_outgoing = delegateExecution.getVariable("identifierServiceName_amdocs_outgoing").toString();
                }
                if ("orga".equals(billingTCF)) {
                    tcfFormId = delegateExecution.getVariable("orgaTcfFormId").toString();
                    identifierServiceName_orga_outgoing = delegateExecution.getVariable("identifierServiceName_orga_outgoing").toString();
                }
                serviceTypeId = "177";
            }
            StringBuilder response = new StringBuilder();
            Authenticator.setDefault(new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("kcell.kz\\" + username, pwd.toCharArray());
                }
            });

            URL urlRequest = new URL(baseUri + "/Lists/getbytitle('" + sharepointUrlPart + "')/items(" + tcfFormId + ")");
            HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json;odata=verbose");

            InputStream stream = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String str = "";
            while ((str = in.readLine()) != null) {
                response.append(str);
            }
            in.close();

            delegateExecution.setVariable(billingTCF + "GetResponseBodyTCF", response.toString());

            JSONObject responseSharepointJSON = new JSONObject(response.toString());
            JSONObject tcf = responseSharepointJSON.getJSONObject("d");
            //String Id = tcf.get("Id").toString();
            String Status = tcf.get("Status").toString();
            System.out.println("Status: " + Status);
            if ("Completed".equals(Status)) {
                JSONObject tcfFormJSON = new JSONObject();

                String htmlTable = tcf.get("Requirments").toString();
                Document doc = Jsoup.parse(htmlTable);
                Element table = doc.select("table").get(0);

                long trCount = table.select("tr").size();
                for (int i = 2; i < trCount; i++) {
                    String identifierTCFID = "";
                    Element row = table.select("tr").get(i);
                    Element tdServiceElem = row.select("td").get(0);
                    String tdServiceName = tdServiceElem.text();
                    Element tdTitleElem = row.select("td").get(1);
                    String tdTitle = tdTitleElem.text();

                    if (title.equals(tdTitle)) {
                        if ("amdocs".equals(billingTCF)) {
                            if ("bulksmsConnectionKAE".equals(processDefinitionKey)) {
                                if (identifierServiceName_amdocs_incoming != null && identifierServiceName_amdocs_incoming.equals(tdServiceName)) {
                                    Element tdTCFId = row.select("td").get(4);
                                    identifierTCFID = tdTCFId.text();
                                    tcfFormJSON.put("identifierAmdocsID_incoming", identifierTCFID);
                                    delegateExecution.setVariable("identifierAmdocsID_incoming", identifierTCFID);
                                }
                            }
                            if (identifierServiceName_amdocs_outgoing != null && identifierServiceName_amdocs_outgoing.equals(tdServiceName)) {
                                Element tdTCFId = row.select("td").get(4);
                                identifierTCFID = tdTCFId.text();
                                tcfFormJSON.put("identifierAmdocsID_outgoing", identifierTCFID);
                                delegateExecution.setVariable("identifierAmdocsID_outgoing", identifierTCFID);
                            }
                            if (("freephone".equals(processDefinitionKey) && tcfFormJSON.has("identifierAmdocsID_outgoing"))) {
                                String getShortNumberResponse = getShortNumberId(title, serviceTypeId);
                                JSONObject getShortNumberResponseJSON = new JSONObject(getShortNumberResponse);

                                if (getShortNumberResponseJSON.has("id")) {
                                    String shortNumberId = getShortNumberResponseJSON.get("id").toString();
                                    boolean exist = false;
                                    try {
                                        String billing = getBilling(shortNumberId);
                                        JSONArray jsonArray = new JSONArray(billing);
                                        exist = jsonArray.length() > 0;
                                        if (exist) {
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(j);
                                                jsonObject.put("amdocsIdOut", identifierTCFID);
                                                String putVasUrlsResult = putVasUrls(jsonObject.getLong("id") + "", jsonObject.toString());
                                                log.info("putVasUrlsResult");
                                                log.info(putVasUrlsResult);
                                                if (putVasUrlsResult != null) {
                                                    delegateExecution.setVariable("amdocsTcfIdReceived", true);
                                                } else {
                                                    delegateExecution.setVariable("amdocsTcfIdReceived", false);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (!exist) {
                                        JSONObject postShortNumberRequestJSON = new JSONObject();
                                        JSONObject vasShortNumber = new JSONObject();
                                        vasShortNumber.put("id", shortNumberId);
                                        postShortNumberRequestJSON.put("amdocsIdOut", identifierTCFID);
                                        postShortNumberRequestJSON.put("vasShortNumber", vasShortNumber);
                                        postShortNumberRequestJSON.put("cost", 0);
                                        String postVasUrlsResponse = postVasUrls(postShortNumberRequestJSON.toString());
                                        JSONObject postVasUrlsJSON = new JSONObject(postVasUrlsResponse);

                                        if (postVasUrlsJSON.has("id") && postVasUrlsJSON.has("amdocsIdOut")) {
                                            delegateExecution.setVariable("amdocsTcfIdReceived", true);
                                        } else {
                                            delegateExecution.setVariable("amdocsTcfIdReceived", false);
                                        }
                                    }
                                } else {
                                    delegateExecution.setVariable("amdocsTcfIdReceived", false);
                                }
                            } else if ("bulksmsConnectionKAE".equals(processDefinitionKey) && tcfFormJSON.has("identifierAmdocsID_incoming") && tcfFormJSON.has("identifierAmdocsID_outgoing")) {
                                String getShortNumberIncomingResponse = getShortNumberId(title, serviceTypeId);
                                JSONObject shortNumberInJSON = new JSONObject(getShortNumberIncomingResponse);
                                if (shortNumberInJSON.has("id")) {
                                    String shortNumberId = shortNumberInJSON.get("id").toString();
                                    boolean exist = false;
                                    try {
                                        String billing = getVasChargingMtsBilling(shortNumberId);
                                        JSONArray jsonArray = new JSONArray(billing);
                                        exist = jsonArray.length() > 0;
                                        if (exist) {
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(j);
                                                String amdocsIdIn = tcfFormJSON.get("identifierAmdocsID_incoming").toString();
                                                jsonObject.put("amdocsIdIn", amdocsIdIn);
                                                String putVasUrlsResult = putVasChargingMts(jsonObject.getLong("vasChargingMtId") + "", jsonObject.toString());
                                                if (putVasUrlsResult != null) {
                                                    delegateExecution.setVariable("amdocsTcfIdReceived", true);
                                                } else {
                                                    delegateExecution.setVariable("amdocsTcfIdReceived", false);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (!exist) {
                                        JSONObject postShortNumberRequestJSON = new JSONObject();
                                        String amcodsIdIn = tcfFormJSON.get("identifierAmdocsID_incoming").toString();
                                        postShortNumberRequestJSON.put("amdocsIdIn", amcodsIdIn);
                                        postShortNumberRequestJSON.put("vasShorNumberId", Long.parseLong(shortNumberId));
                                        postShortNumberRequestJSON.put("cost", 0);
                                        postShortNumberRequestJSON.put("operatorCost", 100);
                                        postShortNumberRequestJSON.put("part", "L");
                                        postShortNumberRequestJSON.put("id", 0);
                                        String postVasUrlsResponse = postVasChargingMts(postShortNumberRequestJSON.toString());
                                        JSONObject postVasUrlsJSON = new JSONObject(postVasUrlsResponse);

                                        if (postVasUrlsJSON.has("id") && postVasUrlsJSON.has("amdocsIdIn")) {
                                            delegateExecution.setVariable("amdocsTcfIdReceived", true);
                                        } else {
                                            delegateExecution.setVariable("amdocsTcfIdReceived", false);
                                        }
                                    }
                                }

                                String getShortNumberOutgoingResponse = getShortNumberId(title, serviceTypeId);
                                JSONObject shortNumberOutJSON = new JSONObject(getShortNumberOutgoingResponse);
                                if (shortNumberOutJSON.has("id")) {
                                    String shortNumberId = shortNumberInJSON.get("id").toString();
                                    boolean exist = false;
                                    try {
                                        String billing = getBilling(shortNumberId);
                                        JSONArray jsonArray = new JSONArray(billing);
                                        exist = jsonArray.length() > 0;
                                        if (exist) {
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(j);
                                                String amdocsIdOut = tcfFormJSON.get("identifierAmdocsID_outgoing").toString();
                                                jsonObject.put("amdocsIdOut", amdocsIdOut);
                                                String putVasUrlsResult = putVasUrls(jsonObject.getLong("id") + "", jsonObject.toString());
                                                if (putVasUrlsResult != null) {
                                                    delegateExecution.setVariable("amdocsTcfIdReceived", true);
                                                } else {
                                                    delegateExecution.setVariable("amdocsTcfIdReceived", false);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (!exist) {
                                        JSONObject postShortNumberRequestJSON = new JSONObject();
                                        JSONObject vasShortNumber = new JSONObject();
                                        vasShortNumber.put("id", shortNumberId);
                                        String amdocsIdOut = tcfFormJSON.get("identifierAmdocsID_outgoing").toString();
                                        postShortNumberRequestJSON.put("amdocsIdOut", amdocsIdOut);
                                        postShortNumberRequestJSON.put("vasShortNumber", vasShortNumber);
                                        postShortNumberRequestJSON.put("cost", 0);
                                        String postVasUrlsResponse = postVasUrls(postShortNumberRequestJSON.toString());
                                        JSONObject postVasUrlsJSON = new JSONObject(postVasUrlsResponse);
                                        if (postVasUrlsJSON.has("id") && postVasUrlsJSON.has("amdocsIdOut")) {
                                            delegateExecution.setVariable("amdocsTcfIdReceived", true);
                                        } else {
                                            delegateExecution.setVariable("amdocsTcfIdReceived", false);
                                        }
                                    }
                                }
                            } else {
                                delegateExecution.setVariable("amdocsTcfIdReceived", false);
                            }
                            delegateExecution.setVariable("amdocsRejectedFromTCF", false);
                        } else if ("orga".equals(billingTCF)) {
                            if ("bulksmsConnectionKAE".equals(processDefinitionKey)) {
                                if (identifierServiceName_orga_incoming != null && identifierServiceName_orga_incoming.equals(tdServiceName)) {
                                    Element tdTCFId = row.select("td").get(4);
                                    identifierTCFID = tdTCFId.text();
                                    tcfFormJSON.put("identifierOrgaID_incoming", identifierTCFID);
                                    delegateExecution.setVariable("identifierOrgaID_incoming", identifierTCFID);
                                }
                            }
                            if (identifierServiceName_orga_outgoing != null && identifierServiceName_orga_outgoing.equals(tdServiceName)) {
                                Element tdTCFId = row.select("td").get(4);
                                identifierTCFID = tdTCFId.text();
                                tcfFormJSON.put("identifierOrgaID_outgoing", identifierTCFID);
                                delegateExecution.setVariable("identifierOrgaID_outgoing", identifierTCFID);
                            }
                            if ("freephone".equals(processDefinitionKey) && tcfFormJSON.has("identifierOrgaID_outgoing")) {
                                String getShortNumberResponse = getShortNumberId(title, serviceTypeId);
                                JSONObject getShortNumberResponseJSON = new JSONObject(getShortNumberResponse);
                                if (getShortNumberResponseJSON.has("id")) {
                                    String shortNumberId = getShortNumberResponseJSON.get("id").toString();
                                    boolean exist = false;
                                    try {
                                        String billing = getBilling(shortNumberId);
                                        JSONArray jsonArray = new JSONArray(billing);
                                        exist = jsonArray.length() > 0;
                                        if (exist) {
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(j);
                                                jsonObject.put("orgaIdOut", identifierTCFID);
                                                jsonObject.put("cbossIdOut", identifierTCFID.substring(identifierTCFID.lastIndexOf("_") + 1));
                                                String putVasUrlsResult = putVasUrls(jsonObject.getLong("id") + "", jsonObject.toString());
                                                if (putVasUrlsResult != null) {
                                                    delegateExecution.setVariable("orgaTcfIdReceived", true);
                                                } else {
                                                    delegateExecution.setVariable("orgaTcfIdReceived", false);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (!exist) {
                                        JSONObject postShortNumberRequestJSON = new JSONObject();
                                        JSONObject vasShortNumber = new JSONObject();

                                        postShortNumberRequestJSON.put("orgaIdOut", identifierTCFID);
                                        postShortNumberRequestJSON.put("cbossIdOut", identifierTCFID.substring(identifierTCFID.lastIndexOf("_") + 1));

                                        vasShortNumber.put("id", shortNumberId);
                                        postShortNumberRequestJSON.put("vasShortNumber", vasShortNumber);
                                        postShortNumberRequestJSON.put("cost", 0);

                                        String postVasUrlsResponse = postVasUrls(postShortNumberRequestJSON.toString());
                                        JSONObject postVasUrlsJSON = new JSONObject(postVasUrlsResponse);

                                        if (postVasUrlsJSON.has("id") && postVasUrlsJSON.has("orgaIdOut") && postVasUrlsJSON.has("cbossIdOut")) {
                                            delegateExecution.setVariable("orgaTcfIdReceived", true);
                                        } else {
                                            delegateExecution.setVariable("orgaTcfIdReceived", false);
                                        }
                                    }
                                } else {
                                    delegateExecution.setVariable("orgaTcfIdReceived", false);
                                }
                            } else {
                                delegateExecution.setVariable("orgaTcfIdReceived", false);
                            }
                            if ("bulksmsConnectionKAE".equals(processDefinitionKey) && tcfFormJSON.has("identifierOrgaID_incoming") && tcfFormJSON.has("identifierOrgaID_outgoing")) {
                                String getShortNumberIncomingResponse = getShortNumberId(title, serviceTypeId);
                                JSONObject shortNumberInJSON = new JSONObject(getShortNumberIncomingResponse);
                                if (shortNumberInJSON.has("id")) {
                                    String shortNumberId = shortNumberInJSON.get("id").toString();
                                    boolean exist = false;
                                    try {
                                        String billing = getVasChargingMtsBilling(shortNumberId);
                                        JSONArray jsonArray = new JSONArray(billing);
                                        exist = jsonArray.length() > 0;
                                        if (exist) {
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(j);
                                                String orgaIdIn = tcfFormJSON.get("identifierOrgaID_incoming").toString();
                                                jsonObject.put("orgaIdIn", orgaIdIn);
                                                String putVasUrlsResult = putVasChargingMts(jsonObject.getLong("vasChargingMtId") + "", jsonObject.toString());
                                                if (putVasUrlsResult != null) {
                                                    delegateExecution.setVariable("orgaTcfIdReceived", true);
                                                } else {
                                                    delegateExecution.setVariable("orgaTcfIdReceived", false);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (!exist) {
                                        JSONObject postShortNumberRequestJSON = new JSONObject();
                                        String amcodsIdIn = tcfFormJSON.get("identifierOrgaID_incoming").toString();
                                        postShortNumberRequestJSON.put("orgaIdIn", amcodsIdIn);
                                        postShortNumberRequestJSON.put("vasShorNumberId", Long.parseLong(shortNumberId));
                                        postShortNumberRequestJSON.put("cost", 0);
                                        postShortNumberRequestJSON.put("part", "L");
                                        postShortNumberRequestJSON.put("operatorCost", 100);
                                        postShortNumberRequestJSON.put("id", 0);
                                        String postVasUrlsResponse = postVasChargingMts(postShortNumberRequestJSON.toString());
                                        JSONObject postVasUrlsJSON = new JSONObject(postVasUrlsResponse);

                                        if (postVasUrlsJSON.has("id") && postVasUrlsJSON.has("orgaIdIn")) {
                                            delegateExecution.setVariable("orgaTcfIdReceived", true);
                                        } else {
                                            delegateExecution.setVariable("orgaTcfIdReceived", false);
                                        }
                                    }
                                }

                                String getShortNumberOutgoingResponse = getShortNumberId(title, serviceTypeId);
                                JSONObject shortNumberOutJSON = new JSONObject(getShortNumberOutgoingResponse);
                                if (shortNumberOutJSON.has("id")) {
                                    String shortNumberId = shortNumberInJSON.get("id").toString();
                                    boolean exist = false;
                                    try {
                                        String billing = getBilling(shortNumberId);
                                        JSONArray jsonArray = new JSONArray(billing);
                                        exist = jsonArray.length() > 0;
                                        if (exist) {
                                            for (int j = 0; j < jsonArray.length(); j++) {
                                                JSONObject jsonObject = jsonArray.getJSONObject(j);
                                                String orgaIdOut = tcfFormJSON.get("identifierOrgaID_outgoing").toString();
                                                jsonObject.put("orgaIdOut", orgaIdOut);
                                                String putVasUrlsResult = putVasUrls(jsonObject.getLong("id") + "", jsonObject.toString());
                                                if (putVasUrlsResult != null) {
                                                    delegateExecution.setVariable("orgaTcfIdReceived", true);
                                                } else {
                                                    delegateExecution.setVariable("orgaTcfIdReceived", false);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if (!exist) {
                                        JSONObject postShortNumberRequestJSON = new JSONObject();
                                        JSONObject vasShortNumber = new JSONObject();
                                        vasShortNumber.put("id", shortNumberId);
                                        String orgaIdOut = tcfFormJSON.get("identifierOrgaID_outgoing").toString();
                                        postShortNumberRequestJSON.put("orgaIdOut", orgaIdOut);
                                        postShortNumberRequestJSON.put("vasShortNumber", vasShortNumber);
                                        postShortNumberRequestJSON.put("cost", 0);
                                        String postVasUrlsResponse = postVasUrls(postShortNumberRequestJSON.toString());
                                        JSONObject postVasUrlsJSON = new JSONObject(postVasUrlsResponse);
                                        if (postVasUrlsJSON.has("id") && postVasUrlsJSON.has("orgaIdOut")) {
                                            delegateExecution.setVariable("orgaTcfIdReceived", true);
                                        } else {
                                            delegateExecution.setVariable("orgaTcfIdReceived", false);
                                        }
                                    }
                                }
                            }
                            delegateExecution.setVariable("orgaRejectedFromTCF", false);
                        }
                    }
                }
            } else if (Arrays.asList(rejectStatusByTCF).contains(Status)) {
                if ("amdocs".equals(billingTCF)) {
                    delegateExecution.setVariable("amdocsTcfIdReceived", false);
                    delegateExecution.setVariable("amdocsRejectedFromTCF", true);
                }
                if ("orga".equals(billingTCF)) {
                    delegateExecution.setVariable("orgaTcfIdReceived", false);
                    delegateExecution.setVariable("orgaRejectedFromTCF", true);
                }
            } else {
                if ("amdocs".equals(billingTCF)) {
                    delegateExecution.setVariable("amdocsTcfIdReceived", false);
                    delegateExecution.setVariable("amdocsRejectedFromTCF", false);
                }
                if ("orga".equals(billingTCF)) {
                    delegateExecution.setVariable("orgaTcfIdReceived", false);
                    delegateExecution.setVariable("orgaRejectedFromTCF", false);
                }
            }
        } else {
            String responseString = "{\n" +
                "    \"d\": {\n" +
                "        \"__metadata\": {\n" +
                "            \"id\": \"Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)\",\n" +
                "            \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)\",\n" +
                "            \"etag\": \"\\\"21\\\"\",\n" +
                "            \"type\": \"" + sharepointRequestBody + " \"\n" +
                "        },\n" +
                "        \"FirstUniqueAncestorSecurableObject\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/FirstUniqueAncestorSecurableObject\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"RoleAssignments\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/RoleAssignments\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"AttachmentFiles\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/AttachmentFiles\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"ContentType\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/ContentType\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesAsHtml\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/FieldValuesAsHtml\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesAsText\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/FieldValuesAsText\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FieldValuesForEdit\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/FieldValuesForEdit\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"File\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/File\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"Folder\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/Folder\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"ParentList\": {\n" +
                "            \"__deferred\": {\n" +
                "                \"uri\": \"https://sp19.kcell.kz/forms/_api/Web/Lists(guid'd79e9f26-54d0-4db3-9488-d551236b0005')/Items(2914)/ParentList\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"FileSystemObjectType\": 0,\n" +
                "        \"Id\": 2914,\n" +
                "        \"ContentTypeId\": \"0x010067B7029E75B96D43BBF9D557A43A4B5C\",\n" +
                "        \"Title\": \"Form #2914\",\n" +
                "        \"InitiatorId\": 3034,\n" +
                "        \"InitiatorDepartment\": \"B2B\",\n" +
                "        \"Subject\": \"B2B short numbers\",\n" +
                "        \"DateDeadline\": \"2018-06-26T00:00:00Z\",\n" +
                "        \"OldTS\": null,\n" +
                "        \"NewTS\": null,\n" +
                "        \"Operator\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.String)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                \"Activ\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"BillingType\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.String)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                \"Orga\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"Service\": \"Products / Tariffs\",\n" +
                "        \"RelationWithThirdParty\": false,\n" +
                "        \"Requirments\": \"<div class=\\\"ExternalClass13053E1C48BA42E3B105B9FE030144BB\\\">\\n<table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width&#58;493px;\\\">\\n\\t<tbody>\\n\\t\\t<tr>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;99px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>ervice Name </strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;64px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Short Number</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td colspan=\\\"3\\\" style=\\\"width&#58;192px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Orga</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td rowspan=\\\"2\\\" style=\\\"width&#58;139px;height&#58;20px;\\\">\\n\\t\\t\\t<p><strong>Comments for ICTD</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Counter</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Price per counter</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p><strong>Orga ID</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;60px;\\\">\\n\\t\\t\\t<p><strong>Create new name and tariff</strong></p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td colspan=\\\"5\\\" style=\\\"width&#58;395px;height&#58;60px;\\\">\\n\\t\\t\\t<p>&#160;</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Outgoing SMS Resmi 3775</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>3775</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>7 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>VASSMSO_3775_91616</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Please change&#160; tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\tSMS Concult<br>\\n\\t\\t\\t5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>&#160;</p>\\n\\n\\t\\t\\t<table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"width&#58;145px;\\\">\\n\\t\\t\\t\\t<tbody>\\n\\t\\t\\t\\t\\t<tr>\\n\\t\\t\\t\\t\\t\\t<td style=\\\"width&#58;145px;height&#58;19px;\\\">VASSMSO_5190_91693</td>\\n\\t\\t\\t\\t\\t</tr>\\n\\t\\t\\t\\t\\t<tr>\\n\\t\\t\\t\\t\\t\\t<td style=\\\"height&#58;19px;\\\">&#160;</td>\\n\\t\\t\\t\\t\\t</tr>\\n\\t\\t\\t\\t</tbody>\\n\\t\\t\\t</table>\\n\\n\\t\\t\\t<p>&#160;</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Incoming SMS<br>\\n\\t\\t\\tSMS Concult 5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>5190</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASSMSI_5190_91694</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\tPhilip Morris<br>\\n\\t\\t\\t2121</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>2121</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>TuanTuan_2121_3304</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please change&#160; tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;85px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1355 Karagandinskaya Sluzhba Spaseniya</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>1355</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;85px;\\\">\\n\\t\\t\\t<p>VASIVR_1355_91692</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;85px;\\\">\\n\\t\\t\\t<p>Please change tariff from 01.07.2018</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Incoming SMS<br>\\n\\t\\t\\t7111&#160; Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>7111</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Timwe_7111_3621</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Outgoing SMS<br>\\n\\t\\t\\t7111&#160; Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>7111</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>1 sms</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Timwe_7111_3620</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 9595 Kazkomerts bank</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>9595</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;68px;\\\">\\n\\t\\t\\t<p>VASIVR_9595_91695</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;68px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 2777 Bazis A</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>2777</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;34px;\\\">\\n\\t\\t\\t<p>VASIVR_2777_90633</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;34px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1450 Adal Su</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1450</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>15 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASIVR_1450_9944</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please change tariff. First 10 sec free of charge</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t\\t<tr>\\n\\t\\t\\t<td style=\\\"width&#58;99px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Outgoing IVR 1515 &quot;ITS Security Group&quot;</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>1515</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>60 sec</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>0 tg</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;64px;height&#58;51px;\\\">\\n\\t\\t\\t<p>VASIVR_1515_91696</p>\\n\\t\\t\\t</td>\\n\\t\\t\\t<td style=\\\"width&#58;139px;height&#58;51px;\\\">\\n\\t\\t\\t<p>Please&#160; create new name and tariff.</p>\\n\\t\\t\\t</td>\\n\\t\\t</tr>\\n\\t</tbody>\\n</table>\\n</div>\\n\",\n" +
                "        \"StartDate\": null,\n" +
                "        \"EndDate\": null,\n" +
                "        \"ProductName\": null,\n" +
                "        \"ProductComponents\": null,\n" +
                "        \"BalancesPocketsAccums\": null,\n" +
                "        \"Status\": \"Completed\",\n" +
                "        \"Waiting_x0020_forId\": null,\n" +
                "        \"InterconnectPhonesTesting\": null,\n" +
                "        \"InterconnectDateTestStart\": null,\n" +
                "        \"InterconnectDateTestEnd\": null,\n" +
                "        \"CBOSSPhonesTesting\": null,\n" +
                "        \"CBOSSDateTestStart\": null,\n" +
                "        \"CBOSSDateTestEnd\": null,\n" +
                "        \"ORGAPhonesTesting\": \"77010100756\",\n" +
                "        \"ORGADateTestStart\": \"2018-06-24T00:00:00Z\",\n" +
                "        \"ORGADateTestEnd\": \"2018-06-25T00:00:00Z\",\n" +
                "        \"TICPhonesTesting\": null,\n" +
                "        \"TICDateTestStart\": null,\n" +
                "        \"TICDateTestEnd\": null,\n" +
                "        \"RoamingPhonesTesting\": null,\n" +
                "        \"RoamingDateTestStart\": null,\n" +
                "        \"RoamingDateTestEnd\": null,\n" +
                "        \"Comments\": null,\n" +
                "        \"DepartmentManagerId\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.Int32)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                263\n" +
                "            ]\n" +
                "        },\n" +
                "        \"FD_x002d_BRS_x002d_ExpertId\": 228,\n" +
                "        \"FD_x002d_IBS_x002d_IT_x0020_SupeId\": 2578,\n" +
                "        \"ICTD_x002d_SNS_x002d_BCOU_x002d_Id\": 298,\n" +
                "        \"ICTD_x0020_SpecialistsId\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"Collection(Edm.Int32)\"\n" +
                "            },\n" +
                "            \"results\": [\n" +
                "                222,\n" +
                "                224,\n" +
                "                3291\n" +
                "            ]\n" +
                "        },\n" +
                "        \"DepartmentSpecialistId\": null,\n" +
                "        \"MassApprove\": null,\n" +
                "        \"TCF_CREATE\": {\n" +
                "            \"__metadata\": {\n" +
                "                \"type\": \"SP.FieldUrlValue\"\n" +
                "            },\n" +
                "            \"Description\": \"Completed\",\n" +
                "            \"Url\": \"https://sp13prod.kcell.kz/forms/_layouts/15/wrkstat.aspx?List=d79e9f26-54d0-4db3-9488-d551236b0005&WorkflowInstanceName=70153b0c-4995-4aa4-a208-20b289e67da5\"\n" +
                "        },\n" +
                "        \"TypeForm\": \"»зменение тарифа на существующий сервис (New service TCF)\",\n" +
                "        \"ServiceNameRUS\": \"Free Phone\",\n" +
                "        \"ServiceNameENG\": \"Free Phone\",\n" +
                "        \"ServiceNameKAZ\": \"Free Phone\",\n" +
                "        \"ID\": 2914,\n" +
                "        \"Modified\": \"2018-06-26T09:45:12Z\",\n" +
                "        \"Created\": \"2018-06-19T09:24:51Z\",\n" +
                "        \"AuthorId\": 3034,\n" +
                "        \"EditorId\": 3034,\n" +
                "        \"OData__UIVersionString\": \"1.0\",\n" +
                "        \"Attachments\": false,\n" +
                "        \"GUID\": \"ad7aa207-76aa-4a0b-ba87-684bf9fb0037\"\n" +
                "    }\n" +
                "}";

            JSONObject responseSharepointJSON = new JSONObject(responseString);
            JSONObject tcf = responseSharepointJSON.getJSONObject("d");

            //String Id = tcf.get("Id").toString();
            String htmlTable = tcf.get("Requirments").toString();

            Document doc = Jsoup.parse(htmlTable);
            Element table = doc.select("table").get(0);

            ////////////////////// PARSE HTML /////////////////////////
            /*
            /////// WITHOUT NESTED TABLE ///////////
            Element row = table.select("tr").get(3);
            Element td = row.select("td").get(4);
            String identifierTCFID = td.text();
            */

            /////// WITH NESTED TABLE ///////////
            Element row = table.select("tr").get(4);
            Element td = row.select("td").get(4);
            Element nestedTable = td.getElementsByTag("table").get(0);
            Element nestedTableRow = nestedTable.select("tr").get(0);
            Element nestedTableRowTd = nestedTableRow.select("td").get(0);
            String identifierTCFID = nestedTableRowTd.text();
            ///////////////////////////////////////////////////////////

            if ("amdocs".equals(billingTCF)) {
                delegateExecution.setVariable("identifierAmdocsID", identifierTCFID);
                delegateExecution.setVariable("amdocsTcfIdReceived", true);
                delegateExecution.setVariable("amdocsRejectedFromTCF", false);
            } else {
                delegateExecution.setVariable("amdocsTcfIdReceived", false);
                delegateExecution.setVariable("amdocsRejectedFromTCF", false);
            }

            if ("orga".equals(billingTCF)) {
                delegateExecution.setVariable("identifierOrgaID", identifierTCFID);
                delegateExecution.setVariable("orgaTcfIdReceived", true);
                delegateExecution.setVariable("orgaRejectedFromTCF", false);
            } else {
                delegateExecution.setVariable("orgaTcfIdReceived", false);
                delegateExecution.setVariable("orgaRejectedFromTCF", false);
            }

        }

    }
}
