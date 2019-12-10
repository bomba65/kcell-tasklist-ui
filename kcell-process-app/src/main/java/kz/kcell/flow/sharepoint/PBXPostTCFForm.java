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
import java.util.List;

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

    @Autowired
    RepositoryService repositoryService;
    @Autowired
    private TaskService taskService;

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

        System.out.println("postItemsResponse URL: " + urlStr);
        System.out.println("postItemsResponse BODY: " + requestBodyStr);

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
            if (isSftp) {
                String processKey = repositoryService.createProcessDefinitionQuery().processDefinitionId(delegateExecution.getProcessDefinitionId()).list().get(0).getKey();
                JSONObject customerInformationJSON = new JSONObject(String.valueOf(delegateExecution.getVariable("customerInformation")));
                JSONObject technicalSpecificationsJSON = new JSONObject(String.valueOf(delegateExecution.getVariable("technicalSpecifications")));

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                DateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

                System.err.println("processKey: " + processKey);
                JSONObject requestBodyJSON = new JSONObject();
                JSONObject metadataBodyJSON = new JSONObject("{\"type\": \"SP.Data.ICTD_x0020_TCFListItem\"}");
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
                requestBodyJSON.put("Subject", "PBX " + customerInformationJSON.get("ticName").toString());
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
                requestBodyJSON.put("TypeForm", "Изменение тарифа на существующий сервис (Old service TCF)");
                requestBodyJSON.put("DateDeadline", df.format(((Date) delegateExecution.getVariable("tcfRequestDeadline"))));

                Calendar calendar = Calendar.getInstance();
                int lastDate = calendar.getActualMaximum(Calendar.DATE);
                calendar.set(Calendar.DATE, lastDate);

                Calendar calendarNext = Calendar.getInstance();
                calendarNext.setTime(calendar.getTime());
                calendarNext.add(Calendar.DATE, 1);

                Calendar firstDate = Calendar.getInstance();
                firstDate.set(Calendar.DAY_OF_MONTH, 1);

                Boolean firstDayConnection = (Boolean) delegateExecution.getVariable("firstDayConnection");
                if (firstDayConnection == null) {
                    firstDayConnection = false;
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
                        if (taskName.equals("Confirm commercial starting service")) {
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
                String international = "CLOSED";
                System.out.println("TARIFF:");
                System.out.println("'" + delegateExecution.getVariable("tariff").toString() + "'");
                String internationalVariable = !technicalSpecificationsJSON.has("intenationalCallAccess") ? "No" : technicalSpecificationsJSON.get("intenationalCallAccess").toString();
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес стандарт")) {
                    a = "-";
                    b = "-";
                    c = "-";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Стартовый 5")) {
                    a = "5,000";
                    b = "10.00";
                    c = "555";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Стартовый 10")) {
                    a = "10,000";
                    b = "10.00";
                    c = "1,250";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 30")) {
                    a = "30,000";
                    b = "10.00";
                    c = "3,000";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 50")) {
                    a = "50,000";
                    b = "9.00";
                    c = "5,556";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 100")) {
                    a = "100,000";
                    b = "8.00";
                    c = "12,500";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 150")) {
                    a = "150,000";
                    b = "7.50";
                    c = "20,000";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 200")) {
                    a = "200,000";
                    b = "7.00";
                    c = "28,571";
                    cug = "11";
                    onnet = "11";
                    offnet = "11";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }//Бизнес пакет 300
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 300")) {
                    a = "300,000";
                    b = "7.00";
                    c = "42,857";
                    cug = "10";
                    onnet = "10";
                    offnet = "10";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 400")) {
                    a = "400,000";
                    b = "7.00";
                    c = "57,143";
                    cug = "10";
                    onnet = "10";
                    offnet = "10";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 600")) {
                    a = "600,000";
                    b = "7.00";
                    c = "85,714";
                    cug = "10";
                    onnet = "10";
                    offnet = "10";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 700")) {
                    a = "700,000";
                    b = "7.00";
                    c = "100,000";
                    cug = "9";
                    onnet = "9";
                    offnet = "9";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 1MLN")) {
                    a = "1,000,000";
                    b = "7.00";
                    c = "142,857";
                    cug = "9";
                    onnet = "9";
                    offnet = "9";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 1.5MLN")) {
                    a = "1,500,000";
                    b = "6.00";
                    c = "250,000";
                    cug = "8";
                    onnet = "8";
                    offnet = "8";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 2.5MLN")) {
                    a = "2,500,000";
                    b = "6.00";
                    c = "416,667";
                    cug = "8";
                    onnet = "8";
                    offnet = "8";
                    pstn = "15";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Бизнес пакет 3MLN")) {
                    a = "3,000,000";
                    b = "6.00";
                    c = "500,000";
                    cug = "7";
                    onnet = "7";
                    offnet = "7";
                    pstn = "15";
                    international = internationalVariable.equals("No") ? "CLOSED" : "PBX-ZVONKI-BEZ-GRANIC";
                }
                if (delegateExecution.getVariable("tariff").toString().equals("Нестандартный пакет")) {
                    a = delegateExecution.getVariable("tariffMonthly").toString();
                    b = "6.00";
                    c = delegateExecution.getVariable("tariffBonusMinutes") != null ? delegateExecution.getVariable("tariffBonusMinutes").toString() : "0";
                    cug = delegateExecution.getVariable("tariffCug").toString();
                    onnet = delegateExecution.getVariable("tariffOnnet").toString();
                    offnet = delegateExecution.getVariable("tariffOffnet").toString();
                    pstn = delegateExecution.getVariable("tariffPstn").toString();
                    international = delegateExecution.getVariable("tariffInternational").toString();
                }
                String htmlTemplateTCF = "<table style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">\n" +
                    (!firstDayConnection ? ("  <tr>\n" +
                        "    <td colspan=\"3\">To set the following tariffs for " + customerInformationJSON.get("ticName").toString() + " contract from " + sdf.format(commercialDate) +
                        " commercial_starting_service to " + sdf.format(calendar.getTime()) + ":</td>\n" +
                        "  </tr>\n" +
                        "  <tr>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Direction</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Tariff, incl. VAT</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Charging interval</td>\n" +
                        "  </tr>\n" +
                        "  <tr>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Kcell, Activ</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">11</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                        "  </tr>\n" +
                        "  <tr>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">CUG</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">11</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                        "  </tr>\n" +
                        "  <tr>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Other mobile operators</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">11</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                        "  </tr>\n" +
                        "  <tr>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Fixed network</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">15</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                        "  </tr>\n" +
                        "  <tr>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">International</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + international + "</td>\n" +
                        "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">1 sec</td>\n" +
                        "  </tr>\n" +
                        "  <tr>\n") : "") +

                    (firstDayConnection ?
                        ("    <td colspan=\"3\" style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">To set the following tariffs for " + customerInformationJSON.get("ticName").toString() + " from " + sdf.format(firstDate.getTime()) + ":</td>\n") :
                        ("    <td colspan=\"3\" style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">Please set a new tariff for " + customerInformationJSON.get("ticName").toString() + " from " + sdf.format(calendarNext.getTime()) + ":</td>\n")
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
                    "    <td style=\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;\">" + (international != null && !international.isEmpty() ? international : "CLOSED") + "</td>\n" +
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
                            String responseText = postItemsResponse(baseUri + "/Lists/getbytitle('ICTD%20TCF')/items", "kcell.kz", username, pwd, contextInfoJSON.get("FormDigestValue").toString(), requestBodyJSON.toString());
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
