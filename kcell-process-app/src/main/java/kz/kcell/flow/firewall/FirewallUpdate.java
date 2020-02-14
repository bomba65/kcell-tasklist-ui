package kz.kcell.flow.firewall;

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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import static kz.kcell.flow.firewall.FirewallCreate.*;

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
        String firewallUrl = "192.168.202.67";
        String firewallGroupName = "";
        String companyLatName = "";
        String ipNumber = "";
        String newIpNumber;
        String processDefKey = (delegateExecution.getProcessEngineServices().getRepositoryService().getProcessDefinition(delegateExecution.getProcessDefinitionId()).getKey());

        if(processDefKey.equals("freephone")){
            companyLatName = delegateExecution.getVariable("clientCompanyLatName").toString();
            ipNumber = delegateExecution.getVariable("ipNumber").toString();
            newIpNumber = delegateExecution.getVariable("newIpNumber").toString();
        } else if (processDefKey.equals("PBX")) {
            ipNumber = delegateExecution.getVariable("ipNumberMail").toString();

            JSONObject customerInformation = new JSONObject(delegateExecution.getVariable("customerInformation").toString());
            String companyLatNameWSpace = customerInformation.getString("ticName");
            companyLatName = companyLatNameWSpace.replaceAll("\\s", "_");
            System.out.println("Company w/o spaces" + companyLatName);

            JSONObject sipProtocol = new JSONObject(delegateExecution.getVariable("sipProtocol").toString());
            System.out.println("SIP PROTOCOL -- - -" + sipProtocol);
            String authorizationType = sipProtocol.getString("authorizationType");
            newIpNumber = sipProtocol.getString("ipVoiceTraffic");
            JSONObject technicalSpecifications = new JSONObject(delegateExecution.getVariable("technicalSpecifications").toString());
            String connectionPoint = technicalSpecifications.getString("connectionPoint");
            int virtualNumbersCount = technicalSpecifications.getInt("virtualNumbersCount");

            if(connectionPoint.equals("SIP Proxy") && authorizationType.equals("SIP-транк(доступ по стат. IP)")) {
                firewallGroupName = "OPENSIP_CLIENTS_STATIC";
            } else if (connectionPoint.equals("SIP Proxy") && authorizationType.equals("SIP-авторизация(доступ по стат. IP c лог/пар)")) {
                firewallGroupName = "OPENSIP_CLIENTS";
            } else if (connectionPoint.equals("Asterisk")) {
                firewallGroupName = "SIP_Asterisk_195.47.255.212";
            } else if (connectionPoint.equals("SBC") && (virtualNumbersCount > 0 && virtualNumbersCount < 100)) {
                firewallGroupName = "Acme_Packet_195.47.255.119";
            } else if (connectionPoint.equals("SBC") && (virtualNumbersCount > 99 && virtualNumbersCount < 1000)) {
                firewallGroupName = "Acme_VoIP_195.47.255.84";
            } else if (connectionPoint.equals("SBC") && virtualNumbersCount > 999){
                firewallGroupName = "Acme_VoIP_195.47.255.97";
            } else {
                throw new Exception("UNDEFINED GROUP");
            }

        } else if (processDefKey.equals("bulksmsConnectionKAE")) {
            companyLatName = delegateExecution.getVariable("clientCompanyLatName").toString();
            ipNumber = delegateExecution.getVariable("ipNumber").toString();
            newIpNumber = delegateExecution.getVariable("newIpNumber").toString();
            firewallGroupName = "SMPP_ext_connect_2.78.58.137";
        } else if (processDefKey.equals("revolvingNumbers")) {

            ipNumber = delegateExecution.getVariable("ipNumberMail").toString();

            JSONObject legalInfo = new JSONObject(delegateExecution.getVariable("legalInfo").toString());
            companyLatName = legalInfo.getString("ticName");
            companyLatName.replaceAll("\\s", "_");
            System.out.println("Company w/o spaces" + companyLatName);

            JSONObject technicalSpecifications = new JSONObject(delegateExecution.getVariable("techSpecs").toString());
            JSONObject sip = new JSONObject(technicalSpecifications.get("sip").toString());
            newIpNumber = sip.getString("voiceIP");
            String authorizationType = sip.getString("authorizationType");
            String connectionPoint = sip.getString("connectionPoint");


            if(connectionPoint.equals("SIP Proxy") && authorizationType.equals("SIP-транк(доступ по стат. IP)")) {
                firewallGroupName = "OPENSIP_CLIENTS_STATIC";
            } else if (connectionPoint.equals("SIP Proxy") && authorizationType.equals("SIP-авторизация(доступ по стат. IP c лог/пар)")) {
                firewallGroupName = "OPENSIP_CLIENTS";
            } else if (connectionPoint.equals("Asterisk")) {
                firewallGroupName = "SIP_Asterisk_195.47.255.212";
            } else if (connectionPoint.equals("SBC")) {
                firewallGroupName = "Acme_Packet_195.47.255.119";
            } else {
                throw new Exception("URL is NULL");
            }

        } else {
            throw new Exception("Undefined process defkey" + processDefKey);
        }



        String lastUsedIp = delegateExecution.getVariable("lastUsedIp").toString();
        String lastUsedName = delegateExecution.getVariable("lastUsedName").toString();

        String sid = firewallLogin(firewallUrl);
        delegateExecution.setVariable("newIpNumberMail",newIpNumber);

        delegateExecution.setVariable("firewallProvisioningUpdateSid", sid);
        System.out.println("SID----------------------- " + sid);
//        String tempFirewallName = delegateExecution.getVariable("firewallName").toString();
        String clientCompanyLatNameMail = delegateExecution.getVariable("clientCompanyLatNameMail").toString();
        String newClientCompanyLatNameMail = "";
        if(delegateExecution.hasVariable("addNewHost")){
            if(delegateExecution.getVariable("addNewHost").toString().equals("true")){
                try{
                    System.out.println("addNewHost=>deleting host ");
                    deleteOneGroup(lastUsedName,lastUsedIp,sid,firewallUrl,firewallGroupName);
                    publishHosts(sid,firewallUrl);
                    deleteHost(lastUsedName,lastUsedIp,sid,firewallUrl);
                    publishHosts(sid,firewallUrl);

                } catch (Exception error) {
                    System.out.println("CATCHED EXCEPTION /DELETEHOST ---- " + error);
//                    publishHosts(sid,firewallUrl);

                    logOut(sid, firewallUrl);

                    System.out.println(error.getMessage());

                    throw new Exception(error);
                }
            }
            delegateExecution.removeVariable("addNewHost");
        }

        if(delegateExecution.hasVariable("groupUpdated")){
            if(delegateExecution.getVariable("groupUpdated").toString().equals("true")) {
                try {
                    System.out.println("groupUpdated=>delete last added group");

//                JSONObject hostInfo = getHostInfo(companyLatName,sid,ipNumber,firewallUrl);
                    deleteOneGroup(lastUsedName, lastUsedIp, sid, firewallUrl, firewallGroupName);
                    publishHosts(sid,firewallUrl);
                } catch (Exception error) {
                    System.out.println("CATCHED EXCEPTION /DELETEHOST ---- " + error);
//                    publishHosts(sid,firewallUrl);

                    logOut(sid, firewallUrl);

                    System.out.println(error.getMessage());

                    throw new Exception(error);
                }
            }
            delegateExecution.removeVariable("groupUpdated");
        }

        if(delegateExecution.hasVariable("withoutWork")){
            delegateExecution.removeVariable("withoutWork");
        }

        JSONObject foundInHosts = showAllHosts(sid, newIpNumber, firewallUrl);
        String tempFirewallName;
        if (foundInHosts.getBoolean("foundInFirewall")) {
            Boolean notNeedUpdateFirewall = showHost(sid, foundInHosts, firewallUrl, firewallGroupName);

            foundInHosts.put("notNeedUpdateFirewall", notNeedUpdateFirewall);
            System.out.println("Host have " + firewallGroupName+" group ? - " + notNeedUpdateFirewall);
            tempFirewallName = foundInHosts.getString("name");
            delegateExecution.setVariable("firewallUid",foundInHosts.getString("uid"));
            delegateExecution.setVariable("firewallName",tempFirewallName);
            newClientCompanyLatNameMail=tempFirewallName;

            if (!notNeedUpdateFirewall) {
                try {
                    setHost(tempFirewallName, newIpNumber, sid, firewallUrl, firewallGroupName);
                    String setHostTaskId = publishHosts(sid, firewallUrl);

                    lastUsedName = tempFirewallName;
                    delegateExecution.setVariable("updateFirewallTaskId", setHostTaskId);
                    delegateExecution.setVariable("groupUpdated","true");
                    System.out.println("set variable groupUpdated = true");
                    logOut(sid, firewallUrl);
                    System.out.println("Host ("+ tempFirewallName+") with IP "+ipNumber + " was successfully added to group - " + firewallGroupName);
                } catch (Exception error) {
//                    publishHosts(sid, firewallUrl);
                    logOut(sid, firewallUrl);
                    System.out.println("Host ("+ tempFirewallName+") with IP "+ipNumber + " Catched error on update host to group - " + firewallGroupName);
                    System.out.println(error.getMessage());
                    throw new Exception(error);
                }
            }else {
                delegateExecution.setVariable("withoutWork","true");
                logOut(sid, firewallUrl);
                System.out.println("No need to do anything");
            }
        } else {
            try {
                addHost(companyLatName, newIpNumber , sid, firewallUrl, firewallGroupName);
                String addHostTaskId = publishHosts(sid, firewallUrl);
                lastUsedName = companyLatName + "_" + ipNumber;
                delegateExecution.setVariable("addNewHost","true");
                System.out.println("set variable addNewHost = true");
                newClientCompanyLatNameMail=companyLatName;
                delegateExecution.setVariable("createFirewallTaskId", addHostTaskId);

                logOut(sid, firewallUrl);
                System.out.println("Host was successfully added - " + companyLatName + " IP - " + newIpNumber + " with group " + firewallGroupName);

            } catch (Exception error) {
                System.out.println("Host was not successfully added - " + companyLatName + " IP - " + newIpNumber + " with group " + firewallGroupName + " CATCHED ERROR " + error.getMessage());

//                publishHosts(sid, firewallUrl);
                logOut(sid, firewallUrl);

                throw new Exception(error);
            }
        }
        lastUsedIp = newIpNumber;
        delegateExecution.setVariable("lastUsedName",lastUsedName);
        delegateExecution.setVariable("lastUsedIp",lastUsedIp);
        delegateExecution.setVariable("clientCompanyLatNameMail",clientCompanyLatNameMail);
        delegateExecution.setVariable("newClientCompanyLatNameMail",newClientCompanyLatNameMail);

    }

    static void deleteHost(String companyLatName, String ipNumber, String sid, String firewallUrl) throws Exception {
        TimeUnit.SECONDS.sleep(4);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("name",companyLatName);

        String jsonDelString = objectNode.toString();
        System.out.println("deleteHost JSON as String - " +jsonDelString);
        URL urlRequest2 = new URL("https://"+firewallUrl+":443/web_api/delete-host");
        HttpURLConnection conn2 = (HttpURLConnection) urlRequest2.openConnection();
        conn2.setRequestMethod("POST");
        conn2.setRequestProperty("Accept", "application/json");
        conn2.setRequestProperty("Content-Type", "application/json");
        conn2.setRequestProperty("X-chkp-sid", sid);
        conn2.setDoOutput(true);
        conn2.setDoInput(true);

        try (OutputStream os2 = conn2.getOutputStream()) {
            byte[] input2 = jsonDelString.getBytes(StandardCharsets.UTF_8);
            os2.write(input2, 0, input2.length);
        }

        try (BufferedReader br2 = new BufferedReader(
            new InputStreamReader(conn2.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response2 = new StringBuilder();
            String responseLine2 = null;
            while ((responseLine2 = br2.readLine()) != null) {
                response2.append(responseLine2.trim());
            }
            System.out.println("RESPONSE-delete-HOST" + response2.toString());
        }
        conn2.disconnect();
    }

    static void deleteAllGroups(String companyLatName, String ipNumber, String sid, String firewallUrl) throws Exception {
        TimeUnit.SECONDS.sleep(4);
        JSONArray arr = new JSONArray();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("name",companyLatName);
        objectNode.putArray("groups");
        String jsonDelString = objectNode.toString();
        System.out.println(jsonDelString);
        URL urlRequest2 = new URL("https://"+firewallUrl+":443/web_api/set-host");
        HttpURLConnection conn2 = (HttpURLConnection) urlRequest2.openConnection();
        conn2.setRequestMethod("POST");
        conn2.setRequestProperty("Accept", "application/json");
        conn2.setRequestProperty("Content-Type", "application/json");
        conn2.setRequestProperty("X-chkp-sid", sid);
        conn2.setDoOutput(true);
        conn2.setDoInput(true);

        try (OutputStream os2 = conn2.getOutputStream()) {
            byte[] input2 = jsonDelString.getBytes(StandardCharsets.UTF_8);
            os2.write(input2, 0, input2.length);
        }

        try (BufferedReader br2 = new BufferedReader(
            new InputStreamReader(conn2.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response2 = new StringBuilder();
            String responseLine2 = null;
            while ((responseLine2 = br2.readLine()) != null) {
                response2.append(responseLine2.trim());
            }
            System.out.println("RESPONSE-delete-HOST" + response2.toString());
        }
        conn2.disconnect();
    }

        static void deleteOneGroup(String companyLatName, String ipNumber, String sid, String firewallUrl, String firewallGroupName) throws Exception {
            TimeUnit.SECONDS.sleep(4);


            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode objectNode = objectMapper.createObjectNode();

            objectNode.put("name",companyLatName);
            objectNode.put("ip-address", ipNumber);

            ObjectNode groupsJson = objectMapper.createObjectNode();
            groupsJson.put("remove",firewallGroupName);

            objectNode.set("groups",groupsJson);

            URL urlRequest2 = new URL("https://"+firewallUrl+":443/web_api/set-host");
            HttpURLConnection conn2 = (HttpURLConnection) urlRequest2.openConnection();
            conn2.setRequestMethod("POST");
            conn2.setRequestProperty("Accept", "application/json");
            conn2.setRequestProperty("Content-Type", "application/json");
            conn2.setRequestProperty("X-chkp-sid", sid);
            conn2.setDoOutput(true);
            conn2.setDoInput(true);

            String jsonAddString = objectNode.toString();
            System.out.println("Delete one group JSON string - " + jsonAddString);
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
                System.out.println("Delete one group RESPONSE" + response2.toString());
            }
            conn2.disconnect();

    }

    static JSONObject getHostInfo(String companyLatName, String sid, String ipNumber, String firewallUrl) throws Exception {
        TimeUnit.SECONDS.sleep(4);
        JSONObject returnJson = new JSONObject();
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
            objectNode.put("name",companyLatName + "_test_"+ipNumber);

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
                returnJson = new JSONObject(response.toString());

            }
            conn.disconnect();

        return returnJson;

    }
}

