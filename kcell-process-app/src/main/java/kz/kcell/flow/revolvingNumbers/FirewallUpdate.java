package kz.kcell.flow.pbx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

//
@Service("FirewallUpdate")
@Log

public class FirewallUpdate implements JavaDelegate {

    static {
        disableSslVerification();
    }

    private static void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
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
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        String firewallUrl;
        String companyLatName = String.valueOf(delegateExecution.getVariable("clientCompanyLatName"));
        String ipNumber = String.valueOf(delegateExecution.getVariable("ipNumber"));

        JSONObject sipProtocol = new JSONObject(delegateExecution.getVariable("sipProtocol"));
        String authorizationType = sipProtocol.getString("authorizationType");

        JSONObject technicalSpecifications = new JSONObject(delegateExecution.getVariable("technicalSpecifications"));
        String connectionPoint = technicalSpecifications.getString("connectionPoint");
        int virtualNumbersCount = technicalSpecifications.getInt("virtualNumbersCount");

        if(connectionPoint.equals("SIP Proxy") && authorizationType.equals("SIP-транк (доступ по стат. IP)")) {
            firewallUrl = "2.78.58.154";
        } else if (connectionPoint.equals("SIP Proxy") && authorizationType.equals("SIP-авторизация (доступ по стат. IP c лог/пар)")) {
            firewallUrl = "2.78.58.167";
        } else if (connectionPoint.equals("Asterisk")) {
            firewallUrl = "195.47.255.212";
        } else if (connectionPoint.equals("SBC") && (virtualNumbersCount > 0 && virtualNumbersCount < 100)) {
            firewallUrl = "195.47.255.119";
        } else if (connectionPoint.equals("SBC") && (virtualNumbersCount > 99 && virtualNumbersCount < 1000)) {
            firewallUrl = "195.47.255.84";
        } else if (connectionPoint.equals("SBC") && virtualNumbersCount > 999){
            firewallUrl = "195.47.255.97";
        } else {
            throw new Exception("URL is NULL");
        }

        String sid = firewallLogin(firewallUrl);
        System.out.println("SID----------------------- " + sid);
        JSONObject foundInHosts = (JSONObject) showAllHosts(sid, ipNumber, firewallUrl);
        System.out.println("findIpInHosts return " + foundInHosts);

        if (foundInHosts.getBoolean("foundInFirewall")) {
            Boolean notNeedUpdateFirewall = showHost(sid,foundInHosts, firewallUrl);

            foundInHosts.put("notNeedUpdateFirewall",notNeedUpdateFirewall);
            System.out.println("notNeedUpdateFirewall return - " + notNeedUpdateFirewall);

            if(!notNeedUpdateFirewall){
                try{
                    setHost(companyLatName, ipNumber, sid, firewallUrl);
                    System.out.println("SETHOST----------------------- ");

                    String setHostTaskId = publishHosts(sid, firewallUrl);
                    System.out.println("PUBLISH----------------------- ");

                    delegateExecution.setVariable("updateFirewallTaskId",setHostTaskId);

                    logOut(sid, firewallUrl);

                } catch (Exception error) {
                    System.out.println("CATCHED EXCEPTION /SETHOST ---- " + error);
                    logOut(sid, firewallUrl);
                    throw new Exception(error);
                }
            }
        } else {
            try{
                System.out.println("--------------------------------------");

                addHost(companyLatName, ipNumber, sid, firewallUrl);
                System.out.println("ADDHOST--------------------------------------");

                String addHostTaskId = publishHosts(sid, firewallUrl);
                System.out.println("PUBLISH CREATE--------------------------------------");

                delegateExecution.setVariable("createFirewallTaskId",addHostTaskId);
                logOut(sid, firewallUrl);
            } catch (Exception error){
                System.out.println("CATCHED EXCEPTION /ADDHOST ---- " + error);
                logOut(sid, firewallUrl);
                throw new Exception(error);
            }
        }
    }

    private static String firewallLogin(String firewallUrl) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("user","APIuser");
        objectNode.put("password","API789!");

        String jsonInputString = objectNode.toString();
        URL urlRequest = new URL("https://"+firewallUrl+":443/web_api/login");
        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        BufferedReader bR = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = "";

        StringBuilder responseStrBuilder = new StringBuilder();
        while ((line = bR.readLine()) != null) {

            responseStrBuilder.append(line);
        }

        JSONObject result = new JSONObject(responseStrBuilder.toString());
        String sid = (String) result.get("sid");

        conn.disconnect();
        return sid;
    }

    private static String publishHosts(String sid, String firewallUrl) throws IOException {
        URL urlRequest3 = new URL("https://"+firewallUrl+":443/web_api/publish");
        HttpURLConnection conn3 = (HttpURLConnection) urlRequest3.openConnection();
        conn3.setRequestMethod("POST");
        conn3.setRequestProperty("Accept", "application/json");
        conn3.setRequestProperty("Content-Type", "application/json");
        conn3.setRequestProperty("X-chkp-sid", sid);
        conn3.setDoOutput(true);
        conn3.setDoInput(true);

        String jsonPublish = "{}";

        try (OutputStream os3 = conn3.getOutputStream()) {
            byte[] input3 = jsonPublish.getBytes(StandardCharsets.UTF_8);
            os3.write(input3, 0, input3.length);
        }
        String createFirewallTaskId = null;
        try (BufferedReader br3 = new BufferedReader(
            new InputStreamReader(conn3.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response3 = new StringBuilder();
            String responseLine3 = null;
            while ((responseLine3 = br3.readLine()) != null) {
                response3.append(responseLine3.trim());
            }
            JSONObject jsonResult3 = new JSONObject(response3.toString());
            System.out.println("RESPONSE-PUBLISH" + response3.toString());
            createFirewallTaskId = jsonResult3.get("task-id").toString();
        }
        conn3.disconnect();
        return createFirewallTaskId;
    }

    private static void addHost(String companyLatName, String ipNumber, String sid, String firewallUrl) throws Exception {

//        String jsonAddString = "{\"name\":\"" + companyLatName + "test\",\"ip-address\":\"" + ipNumber + "\",\"groups\":\"APItest_group\"}";
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("name",companyLatName + "_test");
        objectNode.put("ip-address", ipNumber);
//        objectNode.put("groups","APItest_group");

        String jsonAddString = objectNode.toString();
        System.out.println("JSoN ADD ------------------------- " +jsonAddString);
        URL urlRequest2 = new URL("https://"+firewallUrl+":443/web_api/add-host");
        HttpURLConnection conn2 = (HttpURLConnection) urlRequest2.openConnection();
        conn2.setRequestMethod("POST");
        conn2.setRequestProperty("Accept", "application/json");
        conn2.setRequestProperty("Content-Type", "application/json");
        conn2.setRequestProperty("X-chkp-sid", sid);
        conn2.setDoOutput(true);
        conn2.setDoInput(true);

        try (OutputStream os2 = conn2.getOutputStream()) {
            byte[] input2 = jsonAddString.getBytes(StandardCharsets.UTF_8);
            os2.write(input2, 0, input2.length);
        }

        try (BufferedReader br2 = new BufferedReader(
            new InputStreamReader(conn2.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response2 = new StringBuilder();
            String responseLine2 = null;
            while ((responseLine2 = br2.readLine()) != null) {
                response2.append(responseLine2.trim());
            }
            System.out.println("RESPONSE-ADD-HOST" + response2.toString());
        }
        conn2.disconnect();
    }

    private static void logOut(String sid, String firewallUrl) throws Exception {
        URL urlRequestForLogout = new URL("https://"+firewallUrl+":443/web_api/logout");
        HttpURLConnection connForLogout = (HttpURLConnection) urlRequestForLogout.openConnection();
        connForLogout.setRequestMethod("POST");
        connForLogout.setRequestProperty("Accept", "application/json");
        connForLogout.setRequestProperty("Content-Type", "application/json");
        connForLogout.setRequestProperty("X-chkp-sid", sid);
        connForLogout.setDoOutput(true);
        connForLogout.setDoInput(true);

        String jsonLogout = "{}";

        try (OutputStream os4 = connForLogout.getOutputStream()) {
            byte[] input4 = jsonLogout.getBytes(StandardCharsets.UTF_8);
            os4.write(input4, 0, input4.length);
        }

        try (BufferedReader br4 = new BufferedReader(
            new InputStreamReader(connForLogout.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder responseLogout = new StringBuilder();
            String responseLine4 = null;
            while ((responseLine4 = br4.readLine()) != null) {
                responseLogout.append(responseLine4.trim());
            }
            System.out.println("RESPONSE4Logout" + responseLogout.toString());
        }
        connForLogout.disconnect();
    }

    private static Object showAllHosts(String sid, String ipNumber, String firewallUrl) throws Exception {
        int total = 1000;
        JSONObject returnJson = new JSONObject();
        boolean foundInFirewall = false;
        for (int i = 0; i <= total; i = i + 500) {
            URL urlRequest = new URL("https://"+firewallUrl+":443/web_api/show-hosts");
            HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-chkp-sid", sid);
            conn.setDoOutput(true);
            conn.setDoInput(true);

            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put("limit",500);
            objectNode.put("offset",i);
            objectNode.put("details-level","standard");

            String jsonShow = objectNode.toString();
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonShow.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                JSONObject jsonResult = new JSONObject(response.toString());
                JSONArray jsonObjects = (JSONArray) jsonResult.get("objects");


                if (i == 0) {
                    total = (int) jsonResult.get("total");
                }
                for (int j = 0; j < jsonObjects.length(); j++) {
                    JSONObject singleJson = jsonObjects.getJSONObject(j);
                    if(singleJson.has("ipv4-address")){
                        if (singleJson.get("ipv4-address").toString().equals(ipNumber)) {
                            returnJson = singleJson;
                            foundInFirewall = true;
                        }
                    }


                }
            }
            conn.disconnect();
        }
        returnJson.put("foundInFirewall",foundInFirewall);
        System.out.println("foundInFirewall ------------------ " + returnJson);
        return returnJson;

    }

    private static Boolean showHost(String sid, JSONObject foundInHosts, String firewallUrl) throws IOException {

        URL urlRequest = new URL("https://"+firewallUrl+":443/web_api/show-host");
        HttpURLConnection conn = (HttpURLConnection) urlRequest.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-chkp-sid", sid);
        conn.setDoOutput(true);
        conn.setDoInput(true);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("name",foundInHosts.getString("name"));

        String jsonShow = objectNode.toString();

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonShow.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            JSONObject jsonResult = new JSONObject(response.toString());
            JSONArray jsonGroups = (JSONArray) jsonResult.get("groups");
            System.out.println("JSONGROUPS"+jsonGroups);
            if(jsonGroups.length()>0){
                conn.disconnect();
                return true;
            } else {
                conn.disconnect();
                return false;
            }
        }

    }

    private static void setHost(String companyLatName, String ipNumber, String sid, String firewallUrl) throws Exception {

        String jsonAddString = "{\"name\":\"" + companyLatName + "test\",\"ip-address\":\"" + ipNumber + "\",\"groups\":{\"add\":\"APItest_group\"}}";

        URL urlRequest2 = new URL("https://"+firewallUrl+":443/web_api/set-host");
        HttpURLConnection conn2 = (HttpURLConnection) urlRequest2.openConnection();
        conn2.setRequestMethod("POST");
        conn2.setRequestProperty("Accept", "application/json");
        conn2.setRequestProperty("Content-Type", "application/json");
        conn2.setRequestProperty("X-chkp-sid", sid);
        conn2.setDoOutput(true);
        conn2.setDoInput(true);

        try (OutputStream os2 = conn2.getOutputStream()) {
            byte[] input2 = jsonAddString.getBytes(StandardCharsets.UTF_8);
            os2.write(input2, 0, input2.length);
        }

        try (BufferedReader br2 = new BufferedReader(
            new InputStreamReader(conn2.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response2 = new StringBuilder();
            String responseLine2 = null;
            while ((responseLine2 = br2.readLine()) != null) {
                response2.append(responseLine2.trim());
            }
            System.out.println("RESPONSE-SET-HOST" + response2.toString());
        }
        conn2.disconnect();
    }


}

