package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

@Log
@RestController
@RequestMapping("/ntlm")
public class NtlmCheckController {

    @RequestMapping(value = "/check", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getNtlm() {

        String result = "error";
        try {
            String responseText = getAuthenticatedResponse("https://sp.kcell.kz/forms/_api/Lists/getbytitle('TCF_test')/items(1)", "kcell.kz", "camunda_sharepoint", "Bn12#Qaz");
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
            String responseText = postAuthenticatedResponse("https://sp.kcell.kz/forms/_api/contextinfo", "kcell.kz", "camunda_sharepoint", "Bn12#Qaz");
            result = responseText;
        }catch(Exception e){
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/items", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> postNtlmItem(@RequestBody String requestBody) {
        StringEntity requestBodyData = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        JSONObject reqObj = new JSONObject(requestBodyData.toString());

        String result = "error";
        try {
            String responseText = postItemsResponse("https://sp.kcell.kz/forms/_api/contextinfo", "kcell.kz", "camunda_sharepoint", "Bn12#Qaz", reqObj.get("FormDigestValue").toString());
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

        //StringBuilder response = new StringBuilder();

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

        /*
        URL urlRequest = new URL(urlStr);
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("name", "Freddie the Fish");
        params.put("email", "fishie@seamail.example.com");
        params.put("reply_to_thread", 10394);
        params.put("message", "Shark attacks in Botany Bay have gotten out of control. We need more defensive dolphins to protect the schools here, but Mayor Porpoise is too busy stuffing his snout with lobsters. He's so shellfish.");

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json;odata=verbose");
        conn.setRequestProperty("Content-Type", "application/json;odata=verbose");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        for (int c; (c = in.read()) >= 0;)
            response.append((char)c);

        return response.toString();
        */
    }

    private static String postItemsResponse(
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
            "        \"type\": \"SP.Data.TCF_x005f_testListItem\"\n" +
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
}
