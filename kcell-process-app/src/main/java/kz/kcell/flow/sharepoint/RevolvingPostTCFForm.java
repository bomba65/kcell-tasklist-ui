package kz.kcell.flow.sharepoint;


import lombok.extern.java.Log;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service("revolvingPostTCFForm")
@Log
public class RevolvingPostTCFForm implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String baseUri;
    private final String username;
    private final String pwd;
    private final String productCatalogUrl;
    private final String productCatalogAuth;

    @Autowired
    RepositoryService repositoryService;

    @Value("${sharepoint.forms.url.part:TCF_test}")
    private String sharepointUrlPart;

    @Value("${sharepoint.forms.requestBody:SP.Data.TCF_x005f_testListItem}")
    private String sharepointRequestBody;

    @Autowired
    public RevolvingPostTCFForm(@Value("${sharepoint.forms.url:https://sp.kcell.kz/forms/_api}") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd,
                                @Value("${product.catalog.url:http://ldb-al-preprod.kcell.kz}") String productCatalogUrl, @Value("${product.catalog.auth:app.camunda.user:Asd123Qwerty!}") String productCatalogAuth) {
        this.baseUri = baseUri;
        this.username = username;
        this.pwd = pwd;
        this.productCatalogUrl = productCatalogUrl;
        this.productCatalogAuth = productCatalogAuth;
    }

    private static String postAuthenticatedResponse(
        final String urlStr, final String domain,
        final String userName, final String password) throws IOException {




        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }




        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        String jsonRequestBody = "{}";
        URL urlRequest = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("Accept", "application/json;odata=verbose");
        conn.setRequestProperty("Content-Type", "application/json;odata=verbose");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        OutputStream os = conn.getOutputStream();
        os.write(jsonRequestBody.getBytes("UTF-8"));
        os.close();

        // read the response
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
        JSONObject jsonObject = new JSONObject(result);


        in.close();
        conn.disconnect();

        return jsonObject.toString();
    }

    private static String postItemsResponse(
        final String urlStr, final String domain,
        final String userName, final String password,
        final String formDigestValueStr, final String requestBodyStr) throws IOException {

        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        System.out.println("postItemsResponse URL: " + urlStr);
        System.out.println("postItemsResponse BODY: " + requestBodyStr);

        URL urlRequest = new URL(urlStr);





        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }








        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("X-requestDigest", formDigestValueStr);
        conn.setRequestProperty("Accept", "application/json;odata=verbose");
        conn.setRequestProperty("Content-Type", "application/json;odata=verbose");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");

        OutputStream os = conn.getOutputStream();
        os.write(requestBodyStr.getBytes("UTF-8"));
        os.close();

        // read the response
        InputStream in = new BufferedInputStream(conn.getInputStream());
        String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
        System.out.println("postItemsResponse RESPONSE: " + result);
        JSONObject jsonObject = new JSONObject(result);

        in.close();
        conn.disconnect();

        return jsonObject.toString();
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));
            System.out.println("isSftp:");
            System.out.println(isSftp);
            //isSftp = true;
            delegateExecution.removeVariable("tcfIdReceived");
            delegateExecution.removeVariable("rejectedFromTCF");
            if (isSftp) {
                String processKey = repositoryService.createProcessDefinitionQuery().processDefinitionId(delegateExecution.getProcessDefinitionId()).list().get(0).getKey();
                JSONObject legalInformationJSON = new JSONObject(String.valueOf(delegateExecution.getVariable("legalInfo")));
                JSONObject commLaunchTCFJSON = new JSONObject(String.valueOf(delegateExecution.getVariable("commLaunchTCF")));

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                System.err.println("processKey: " + processKey);
                JSONObject requestBodyJSON = new JSONObject();
                JSONObject metadataBodyJSON = new JSONObject("{\"type\": \"" + sharepointRequestBody + "\"}");
                JSONObject operatorBodyJSON = new JSONObject();
                JSONObject departmentManagerIdJSON = new JSONObject();
                JSONObject billingTypeBodyJSON = new JSONObject();
                JSONArray operatorResultsJSONArray = new JSONArray();
                JSONArray billingTypeResultsJSONArray = new JSONArray();
                JSONArray departmentManagerIdJSONArray = new JSONArray();

                requestBodyJSON.put("__metadata", metadataBodyJSON);
                if (delegateExecution.getVariable("starter").toString().equals("Yerlan.Mustafin@kcell.kz")) {

                } else if (Arrays.asList("Sanzhar.Kairolla@kcell.kz", "demo").contains(delegateExecution.getVariable("starter").toString())) {

                }
                requestBodyJSON.put("InitiatorDepartment", "B2B");
                requestBodyJSON.put("Subject", legalInformationJSON.get("ticName").toString());
                operatorResultsJSONArray.put("Telesens");
                billingTypeResultsJSONArray.put("TIC");
                departmentManagerIdJSONArray.put(263);
                operatorBodyJSON.put("results", operatorResultsJSONArray);
                billingTypeBodyJSON.put("results", billingTypeResultsJSONArray);
                departmentManagerIdJSON.put("results", departmentManagerIdJSONArray);
                requestBodyJSON.put("Operator", operatorBodyJSON);
                requestBodyJSON.put("BillingType", billingTypeBodyJSON);
                requestBodyJSON.put("Service", "PBX");
                requestBodyJSON.put("RelationWithThirdParty", false);
                requestBodyJSON.put("DepartmentManagerId", departmentManagerIdJSON);
                requestBodyJSON.put("Status", "Approved by Department Manager");
                requestBodyJSON.put("TypeForm", "Добавление тарифа на новый сервис (Old service TCF)");
                requestBodyJSON.put("DateDeadline", commLaunchTCFJSON.get("deadline").toString());

                Calendar calendar = Calendar.getInstance();
                int lastDate = calendar.getActualMaximum(Calendar.DATE);
                calendar.set(Calendar.DATE, lastDate);

                Calendar calendarNext = Calendar.getInstance();
                calendarNext.setTime(calendar.getTime());
                calendarNext.add(Calendar.DATE, 1);

                Calendar firstDate = Calendar.getInstance();
                firstDate.set(Calendar.DAY_OF_MONTH, 1);

                Boolean firstDayConnection = false;
                if(delegateExecution.getVariable("firstDayConnection")!=null){
                    firstDayConnection = (Boolean) delegateExecution.getVariable("firstDayConnection");
                }

                Date commercialDate = new Date();

                JSONArray jsonArray = new JSONArray(delegateExecution.getVariable("resolutions").toString());
                if (jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String taskName = "";
                        if (jsonObject.has("taskName")) {
                            taskName = jsonObject.getString("taskName");
                        } else if (jsonObject.has("taskId")) {
                            List<Task> taskz = delegateExecution.getProcessEngine().getTaskService().createTaskQuery().taskId(jsonObject.getString("taskId")).list();
                            if (taskz.size() > 0) {
                                taskName = taskz.get(0).getName();
                            }
                        }
                        if (taskName.startsWith("Confirm commercial starting service")) {
                            try {
                                commercialDate = df.parse(jsonObject.getString("taskEndDate"));
                                System.out.println("commercialDate: " + commercialDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if (taskName.equals("Create / Modify request in TCF")) {
                            try {
                                if (jsonObject.has("assigneeName") && (jsonObject.getString("assigneeName").equals("Yerlan Mustafin") || jsonObject.getString("assigneeName").equals("Demo Demo"))) {
                                    requestBodyJSON.put("InitiatorId", "5016");
                                } else if (jsonObject.getString("assigneeName").equals("Sanzhar Kairolla")) {
                                    requestBodyJSON.put("InitiatorId", "4579");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                String a = "";
                String b = "";
                String c = "";
                String cug = "";
                String onnet = "";
                String offnet = "";
                String pstn = "";
                String international = "";
                System.out.println("TARIFF:");
                System.out.println("'" + delegateExecution.getVariable("numbersAmount").toString() + "'");
                if (delegateExecution.getVariable("numbersAmount").toString().equals("60")) {
                    a = "36,000 ₸";
                    b = "-";
                    c = "-";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("numbersAmount").toString().equals("100")) {
                    a = "50,000 ₸";
                    b = "10.00";
                    c = "555";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("numbersAmount").toString().equals("250")) {
                    a = "100,000 ₸";
                    b = "10.00";
                    c = "1,250";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("numbersAmount").toString().equals("500")) {
                    a = "150,000 ₸";
                    b = "10.00";
                    c = "3,000";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("numbersAmount").toString().equals("0")) {
                    a = delegateExecution.getVariable("tariffMonthly").toString();
                    b = "6.00";
                    c = "500,000";
                    cug = delegateExecution.getVariable("tariffCug").toString();
                    onnet = delegateExecution.getVariable("tariffOnnet").toString();
                    offnet = delegateExecution.getVariable("tariffOffnet").toString();
                    pstn = delegateExecution.getVariable("tariffPstn").toString();
                    international = delegateExecution.getVariable("tariffInternational").toString();
                }
                String htmlTemplateTCF = "<table style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">\n" +
                    (!firstDayConnection ? ("  <tr>\n" +
                    "    <td colspan=\"3\">To set the following tariffs for " + legalInformationJSON.get("ticName").toString() + " contract from " + sdf.format(commercialDate) +
                    " commercial_starting_service to " + sdf.format(calendar.getTime()) + ":</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Direction</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Tariff, incl. VAT</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Charging interval</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Kcell, Activ</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + onnet + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">CUG</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + cug + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Other mobile operators</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + offnet + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Fixed network</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + pstn + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">International</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">CLOSED</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n") : "") +

                    (firstDayConnection ?
                        ("    <td colspan=\"3\" style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">To set monthly fee (doesn't depend on voice traffic) for " + legalInformationJSON.get("ticName").toString() + " from " + sdf.format(calendarNext.getTime()) + " - " + a + " tg</td>\n") :
                        ("    <td colspan=\"3\" style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Please set a new tariff for " + legalInformationJSON.get("ticName").toString() + " from " + sdf.format(calendarNext.getTime()) + " - " + a + " tg</td>\n")
                    ) +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Direction</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Tariff, incl. VAT</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Charging interval</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Monthly fee*</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + a + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\"></td>\n" +
                    "  </tr>\n" +
                    "  <tr\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Kcell, Activ</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + onnet + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">CUG</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + cug + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Other mobile operators</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + offnet + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Fixed network</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + pstn + "</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">International</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">CLOSED</td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\"></td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\"></td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\"></td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\" colspan=\"2\">*Monthly fee includes free bonus minutes " + c + " min. </td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\"></td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\"></td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\"></td>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\"></td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\" colspan=\"3\">Monthly fee includes minutes of CUG, onnet, offnet</td>\n" +
                    "  </tr>\n" +
                    "</table>";
                requestBodyJSON.put("Requirments", htmlTemplateTCF);
                System.out.println("Requirments: " + htmlTemplateTCF);
                System.out.println("requestBodyJSON: " + requestBodyJSON);

                String resultContextInfo = "error";
                String resultItems = "error";

                try {
                    String responseText = postAuthenticatedResponse(baseUri + "/contextinfo", "kcell.kz", username, pwd);
                    resultContextInfo = responseText;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("resultContextInfo: " + resultContextInfo);

                if (!"error".equals(resultContextInfo)) {
                    JSONObject contextInfoJSON = new JSONObject(resultContextInfo);
                    System.out.println("contextInfoJSON.has(\"FormDigestValue\")");
                    System.out.println(contextInfoJSON.has("FormDigestValue"));
                    try {
                        contextInfoJSON = contextInfoJSON.getJSONObject("d").getJSONObject("GetContextWebInformation");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (contextInfoJSON.has("FormDigestValue")) {
                        try {
                            String responseText = postItemsResponse(baseUri + "/Lists/getbytitle('" + sharepointUrlPart + "')/items", "kcell.kz", username, pwd, contextInfoJSON.get("FormDigestValue").toString(), requestBodyJSON.toString());
                            resultItems = responseText;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("resultItems: " + resultItems);

                JSONObject responseSharepointJSON = new JSONObject(resultItems);
                String Status = responseSharepointJSON.getJSONObject("d").get("Status").toString();
                delegateExecution.setVariable("tcfFormStatus", Status);
                if (Status.indexOf("Approved") > -1) {
                    delegateExecution.setVariable("tcfFormId", responseSharepointJSON.getJSONObject("d").get("Id").toString());
                    delegateExecution.setVariable("tcfFormIdReceived", true);
                } else {
                    delegateExecution.setVariable("tcfFormIdReceived", false);
                }
            } else {
                delegateExecution.setVariable("tcfFormId", "1111");
                delegateExecution.setVariable("tcfFormIdReceived", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            delegateExecution.setVariable("tcfFormIdReceived", false);
        }
    }
}
