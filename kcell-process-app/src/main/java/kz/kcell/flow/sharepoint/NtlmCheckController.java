package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Arrays;

@Log
@RestController
@RequestMapping("/ntlm")
public class NtlmCheckController {

    @Value("${sharepoint.forms.url.part:TCF_test}")
    private String sharepointUrlPart;

    @Value("${sharepoint.forms.requestBody:SP.Data.TCF_x005f_testListItem}")
    private String sharepointRequestBody;

    @Value("${sharepoint.forms.username:camunda_sharepoint}")
    private String sharepoint_forms_username;

    @Value("${sharepoint.forms.password:camunda_sharepoint:Bn12#Qaz}")
    private String sharepoint_forms_password;

    @RequestMapping(value = "/check", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getNtlm() {

        String result = "error";
        try {
            String responseText = getAuthenticatedResponse("https://sp.kcell.kz/forms/_api/Lists/getbytitle('" + sharepointUrlPart + "')/items(1)", "kcell.kz", sharepoint_forms_username, sharepoint_forms_password);
            result = responseText;
        }catch(Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/contextinfo", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> postNtlmAuth() {

        String result = "error";
        try {
            String responseText = postAuthenticatedResponse("https://sp.kcell.kz/forms/_api/contextinfo", "kcell.kz", sharepoint_forms_username, sharepoint_forms_password);
            result = responseText;
        }catch(Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/testhtml", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> postTestHtml(@RequestBody String requestBody) {
        JSONObject reqObj = new JSONObject(requestBody);

        String result = "error";
        try {
            String responseText = postTestHtmlResponse(requestBody);
            result = responseText;
        }catch(Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/items", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> postNtlmItem(@RequestBody String requestBody) {
        JSONObject reqObj = new JSONObject(requestBody);

        String result = "error";
        try {
            String responseText = postItemsResponse("https://sp.kcell.kz/forms/_api/Lists/getbytitle('" + sharepointUrlPart + "')/items", "kcell.kz", sharepoint_forms_username, sharepoint_forms_password, reqObj.get("FormDigestValue").toString());
            result = responseText;
        }catch(Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    private static String getAuthenticatedResponse(
        final String urlStr, final String domain,
        final String userName, final String password) throws IOException {

        StringBuilder response = new StringBuilder();

        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        URL urlRequest = new URL(urlStr);
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

        return response.toString();
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

    private String postItemsResponse(
        final String urlStr, final String domain,
        final String userName, final String password,
        final String formDigetValueStr) throws IOException {

        Authenticator.setDefault(new Authenticator() {

            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    domain + "\\" + userName, password.toCharArray());
            }
        });

        String jsonRequestBody = "{\n" +
            "    \"__metadata\": {\n" +
            "        \"type\": \"" + sharepointRequestBody + "\"\n" +
            "    },\n" +
            "    \"Subject\": \"B2B Short Numbers\",\n" +
            "    \"DateDeadline\": \"2019-02-21T00:00:00\",\n" +
            "    \"InitiatorDepartment\": \"B2B\",\n" +
            "    \"Operator\": {\n" +
            "        \"results\": [\n" +
            "            \"Activ\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"BillingType\": {\n" +
            "        \"results\": [\n" +
            "            \"Orga\"\n" +
            "        ]\n" +
            "    },\n" +
            "    \"Service\": \"Products/Tariffs\",\n" +
            "    \"RelationWithThirdParty\": false,\n" +
            "    \"Requirments\": \"<div><p></p><table border=\\\"0\\\" cellpadding=\\\"0\\\" cellspacing=\\\"0\\\" style=\\\"border: 1px dotted #d3d3d3;color:#333333;background-color:#ffffff;width:500px;\\\"><tbody><tr><td rowspan=\\\"2\\\" style=\\\"border: 1px dotted #d3d3d3;height: 54px; width: 120px\\\">Service Name</td><td rowspan=\\\"2\\\" style=\\\"border: 1px dotted #d3d3d3; width: 60px\\\">Short Number</td><td colspan=\\\"3\\\" style=\\\"border: 1px dotted #d3d3d3; width: 220px\\\">Orga</td><td rowspan=\\\"2\\\" style=\\\"border: 1px dotted #d3d3d3; width: 100px\\\">Comments for ICTD</td></tr><tr><td style=\\\"border: 1px dotted #d3d3d3; width: 66px\\\">Counter</td><td style=\\\"border: 1px dotted #d3d3d3; width: 66px\\\">Price per counter</td><td style=\\\"border: 1px dotted #d3d3d3;\\\">Orga ID</td></tr><tr><td style=\\\"border: 1px dotted #d3d3d3;\\\">Outgoing SMS Infobip test<br></td><td style=\\\"border: 1px dotted #d3d3d3;\\\">test<br></td><td style=\\\"border: 1px dotted #d3d3d3;\\\">1 sms</td><td style=\\\"border: 1px dotted #d3d3d3;\\\">0 units</td><td style=\\\"border: 1px dotted #d3d3d3;\\\"><br></td><td style=\\\"border: 1px dotted #d3d3d3;\\\"><br></td></tr></tbody></table><p><br></p></div>\",\n" +
            "    \"Status\": \"Approved by Department Manager\",\n" +
            "    \"TypeForm\": \"Изменение тарифа на существующий сервис (New service TCF)\",\n" +
            "    \"ServiceNameRUS\": \"Free Phone / Bulk sms\",\n" +
            "    \"ServiceNameENG\": \"Free Phone / Bulk sms\",\n" +
            "    \"ServiceNameKAZ\": \"Free Phone / Bulk sms\",\n" +
            "    \"Created\": \"2019-02-20T13:25:00Z\"\n" +
            "}";

        URL urlRequest = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setConnectTimeout(5000);
        conn.setRequestProperty("X-requestDigest", formDigetValueStr);
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

    private static String postTestHtmlResponse(
        final String sharepointResponseBody) throws IOException {

        String[] rejectStatusByTCF = { "Canceled", "Rejected by ICTD-SNS-BCOU-BCST Supervisor", "Rejected by ICTD Specialists" };

        JSONObject responseSharepointJSON = new JSONObject(sharepointResponseBody);
        JSONObject jsonObject = new JSONObject();
        JSONObject tcf = responseSharepointJSON.getJSONObject("d");
        //String Id = tcf.get("Id").toString();
        String Status = tcf.get("Status").toString();

        if("Completed".equals(Status)) {
            String htmlTable = tcf.get("Requirments").toString();
            Document doc = Jsoup.parse(htmlTable);
            Element table = doc.select("table").get(0);

            ////////////////////// PARSE HTML /////////////////////////
            /////// WITHOUT NESTED TABLE ///////////
            long trCount = table.select("tr").size();
            for (int i = 2; i < trCount; i++) {
                System.out.println("i = " + i);
                String identifierTCFID = "";
                Element row = table.select("tr").get(i);
                Element tdServiceElem = row.select("td").get(0);
                String tdServiceName = tdServiceElem.text();

                Element tdTCFId = row.select("td").get(4);
                identifierTCFID = tdTCFId.text();

                jsonObject.put(tdServiceName, identifierTCFID);

            }

        }

        return jsonObject.toString();
    }
}
