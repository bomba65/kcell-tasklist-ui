package kz.kcell.flow.sharepoint;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
}
