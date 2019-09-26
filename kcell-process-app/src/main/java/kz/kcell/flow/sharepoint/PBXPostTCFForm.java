package kz.kcell.flow.sharepoint;


import lombok.extern.java.Log;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

@Service("pbxPostTCFForm")
@Log
public class PBXPostTCFForm implements JavaDelegate {
    @Autowired
    private Environment environment;

    private final String baseUri;
    private final String username;
    private final String pwd;
    private final String productCatalogUrl;
    private final String productCatalogAuth;
    Expression billingTCF;

    @Autowired
    RepositoryService repositoryService;

    @Autowired
    public PBXPostTCFForm(@Value("${sharepoint.forms.url:https://sp.kcell.kz/forms/_api}") String baseUri, @Value("${sharepoint.forms.username}") String username, @Value("${sharepoint.forms.password}") String pwd,
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

        log.info("postItemsResponse URL: " + urlStr);
        log.info("postItemsResponse BODY: " + requestBodyStr);

        URL urlRequest = new URL(urlStr);
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
        log.info("postItemsResponse RESPONSE: " + result);
        JSONObject jsonObject = new JSONObject(result);

        in.close();
        conn.disconnect();

        return jsonObject.toString();
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        try {
            Boolean isSftp = Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("sftp")));

            if (isSftp) {
                String processKey = repositoryService.createProcessDefinitionQuery().processDefinitionId(delegateExecution.getProcessDefinitionId()).list().get(0).getKey();
                String billingTCF = this.billingTCF.getValue(delegateExecution).toString();
                JSONObject customerInformationJSON = new JSONObject(String.valueOf(delegateExecution.getVariable("customerInformation")));

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                System.err.println("processKey: " + processKey);
                System.err.println("billingTCF: " + billingTCF);
                JSONObject requestBodyJSON = new JSONObject();
                JSONObject metadataBodyJSON = new JSONObject("{\"type\": \"SP.Data.TCF_x005f_testListItem\"}");
                JSONObject operatorBodyJSON = new JSONObject();
                JSONObject billingTypeBodyJSON = new JSONObject();
                JSONArray operatorResultsJSONArray = new JSONArray();
                JSONArray billingTypeResultsJSONArray = new JSONArray();

                requestBodyJSON.put("__metadata", metadataBodyJSON);
                if (delegateExecution.getVariable("starter").toString().equals("Yerlan.Mustafin@kcell.kz")) {

                } else if (Arrays.asList("Sanzhar.Kairolla@kcell.kz", "demo").contains(delegateExecution.getVariable("starter").toString())) {

                }
                requestBodyJSON.put("InitiatorDepartment", "B2B");
                requestBodyJSON.put("Subject", "PBX " + customerInformationJSON.get("ticName").toString());
                operatorResultsJSONArray.put("Telesens");
                billingTypeResultsJSONArray.put("TIC");
                operatorBodyJSON.put("results", operatorResultsJSONArray);
                billingTypeBodyJSON.put("results", billingTypeResultsJSONArray);
                requestBodyJSON.put("Operator", operatorBodyJSON);
                requestBodyJSON.put("BillingType", billingTypeBodyJSON);
                requestBodyJSON.put("Service", "PBX");
                requestBodyJSON.put("RelationWithThirdParty", false);
                requestBodyJSON.put("DepartmentManagerId", "{\"results\":[263]}");
                requestBodyJSON.put("Status", "Approved by Department Manager");
                requestBodyJSON.put("TypeForm", "Изменение тарифа на существующий сервис (Old service TCF)");
                requestBodyJSON.put("DateDeadline", df.format(((Date) delegateExecution.getVariable("tcfRequestDeadline"))));
                requestBodyJSON.put("Requirements", "");

                Calendar calendar = Calendar.getInstance();
                int lastDate = calendar.getActualMaximum(Calendar.DATE);
                calendar.set(Calendar.DATE, lastDate);

                Calendar calendarNext = Calendar.getInstance();
                calendarNext.setTime(calendar.getTime());
                calendarNext.add(Calendar.DATE, 1);

                Calendar firstDate = Calendar.getInstance();
                firstDate.set(Calendar.DAY_OF_MONTH, 1);

                Boolean firstDayConnection = (Boolean) delegateExecution.getVariable("firstDayConnection");

                Date commercialDate = new Date();
                SpinJsonNode resolutionContainer = delegateExecution.<JsonValue>getVariableTyped("resolutions").getValue();
                if (resolutionContainer.isArray() && resolutionContainer.elements().size() > 0) {
                    SpinList<SpinJsonNode> resolutions = delegateExecution.<JsonValue>getVariableTyped("resolutions").getValue().elements();
                    for (SpinJsonNode resolution : resolutions) {
                        if (resolution.prop("taskName").equals("Confirm commercial starting service")) {
                            try {
                                commercialDate = df.parse(resolution.prop("taskName").stringValue());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else if (resolution.prop("taskName").equals("Create / Modify request in TCF")) {
                            try {
                                if (resolution.prop("assigneeName").equals("Yerlan Mustafin") || resolution.prop("assigneeName").equals("Demo Demo")) {
                                    requestBodyJSON.put("InitiatorId", "5016");
                                } else if (resolution.prop("assigneeName").equals("Sanzhar Kairolla")) {
                                    requestBodyJSON.put("InitiatorId", "4579");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    delegateExecution.setVariable("resolutions", SpinValues.jsonValue(resolutions.toString()));
                }
                String a = "";
                String b = "";
                String c = "";
                String cug = "";
                String onnet = "";
                String offnet = "";
                String pstn = "";
                String international = "";
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес стандарт")) {
                    a = "-";
                    b = "-";
                    c = "-";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 30")) {
                    a = "30,000";
                    b = "10.00";
                    c = "3,000";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 50")) {
                    a = "50,000";
                    b = "9.00";
                    c = "5,556";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 100")) {
                    a = "100,000";
                    b = "8.00";
                    c = "12,500";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 150")) {
                    a = "150,000";
                    b = "7.50";
                    c = "20,000";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 200")) {
                    a = "200,000";
                    b = "7.00";
                    c = "28,571";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 300")) {
                    a = "300,000";
                    b = "7.00";
                    c = "42,857";
                    cug = "10";
                    onnet = "10";
                    offnet = "10";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 400")) {
                    a = "400,000";
                    b = "7.00";
                    c = "57,143";
                    cug = "10";
                    onnet = "10";
                    offnet = "10";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 600")) {
                    a = "600,000";
                    b = "7.00";
                    c = "85,714";
                    cug = "10";
                    onnet = "10";
                    offnet = "10";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 700")) {
                    a = "700,000";
                    b = "7.00";
                    c = "100,000";
                    cug = "9";
                    onnet = "9";
                    offnet = "9";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 1MLN")) {
                    a = "1,000,000";
                    b = "7.00";
                    c = "142,857";
                    cug = "9";
                    onnet = "9";
                    offnet = "9";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 1.5MLN")) {
                    a = "1,500,000";
                    b = "6.00";
                    c = "250,000";
                    cug = "8";
                    onnet = "8";
                    offnet = "8";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 2.5MLN")) {
                    a = "2,500,000";
                    b = "6.00";
                    c = "416,667";
                    cug = "8";
                    onnet = "8";
                    offnet = "8";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 3MLN")) {
                    a = "3,000,000";
                    b = "6.00";
                    c = "500,000";
                    cug = "7";
                    onnet = "7";
                    offnet = "7";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Нестандартный пакет")) {
                    a = "3,000,000";
                    b = "6.00";
                    c = "500,000";
                    cug = delegateExecution.getVariable("tariffCug").toString();
                    onnet = delegateExecution.getVariable("tariffOnnet").toString();
                    offnet = delegateExecution.getVariable("tariffOffnet").toString();
                    pstn = delegateExecution.getVariable("tariffPstn").toString();
                    international = delegateExecution.getVariable("tariffInternational").toString();
                }
                String htmlTemplateTCF = "<table>\n" +
                    "  <tr>\n" +
                    "    <th colspan=\"3\">To set the following tariffs for " + customerInformationJSON.get("ticName").toString() + " contract from " + sdf.format(commercialDate) +
                    " commercial_starting_service to " + sdf.format(calendar.getTime()) + ":</th>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>Direction</td>\n" +
                    "    <td>Tariff, incl. VAT</td>\n" +
                    "    <td>Charging interval</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>Kcell, Activ</td>\n" +
                    "    <td>11<td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>CUG</td>\n" +
                    "    <td>11</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>Other mobile operators</td>\n" +
                    "    <td>11</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>Fixed network</td>\n" +
                    "    <td>15</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>International</td>\n" +
                    "    <td>CLOSED</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +

                    (firstDayConnection ?
                        ("    <td colspan=\"3\">To set the following tariffs for " + customerInformationJSON.get("ticName").toString() + " from 01.08.2019:</td>\n") :
                        ("    <td colspan=\"3\">Please set a new tariff for " + customerInformationJSON.get("ticName").toString() + " from " + sdf.format(calendarNext.getTime()) + ":</td>\n")
                    ) +

                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>Direction</td>\n" +
                    "    <td>Tariff, incl. VAT</td>\n" +
                    "    <td>Charging interval</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>Monthly fee*</td>\n" +
                    "    <td>" + a + "</td>\n" +
                    "    <td></td>\n" +
                    "  </tr>\n" +
                    "  <tr\n" +
                    "  <tr>\n" +
                    "    <td>Kcell, Activ</td>\n" +
                    "    <td>" + onnet + "</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>CUG</td>\n" +
                    "    <td>" + cug + "</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>Other mobile operators</td>\n" +
                    "    <td>" + offnet + "</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>Fixed network</td>\n" +
                    "    <td>" + pstn + "</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td>International</td>\n" +
                    "    <td>" + (international != null && !international.isEmpty() ? international : "CLOSED") + "</td>\n" +
                    "    <td>1 sec</td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td></td>\n" +
                    "    <td></td>\n" +
                    "    <td></td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td colspan=\"2\">*Monthly fee includes free bonus minutes " + c + " min. </td>\n" +
                    "    <td></td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td></td>\n" +
                    "    <td></td>\n" +
                    "    <td></td>\n" +
                    "  </tr>\n" +
                    "  <tr>\n" +
                    "    <td colspan=\"3\">Monthly fee includes minutes of CUG, onnet, offnet</td>\n" +
                    "  </tr>\n" +
                    "</table>";
                requestBodyJSON.put("Requirments", htmlTemplateTCF);

                String resultContexninfo = "error";
                String resultItems = "error";

                try {
                    String responseText = postAuthenticatedResponse(baseUri + "/contextinfo", "kcell.kz", username, pwd);
                    resultContexninfo = responseText;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!"error".equals(resultContexninfo)) {
                    JSONObject contextinfoJSON = new JSONObject(resultContexninfo);
                    if (contextinfoJSON.has("FormDigestValue")) {
                        try {
                            String responseText = postItemsResponse(baseUri + "/Lists/getbytitle('ICTD%20TCF')/items", "kcell.kz", username, pwd, contextinfoJSON.get("FormDigestValue").toString(), requestBodyJSON.toString());
                            resultItems = responseText;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                JSONObject responseSharepointJSON = new JSONObject(resultItems);
                String Status = responseSharepointJSON.get("Status").toString();
                delegateExecution.setVariable("tcfFormStatus", Status);
                if (Status.indexOf("Approved") > -1) {
                    delegateExecution.setVariable("tcfFormId", responseSharepointJSON.get("Id").toString());
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
