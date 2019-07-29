package kz.kcell.flow.aftersales;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service("AftersalesUpdateClientData")
@lombok.extern.java.Log

public class UpdateClientData implements JavaDelegate {

    private static List<String> skippedFields = Arrays.asList(
        "ID",
        "IS_ACTIVE",
        "STATUS",
        "CREATED_BY",
        "DATE_CREATED",
        "IS_DELETED",
        "CLT_NUMBER",
        "IPS",
        "VOICE_PLATFORM"
    );

    @Autowired
    HistoryService historyService;

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

        if (!delegateExecution.hasVariable("clientBIN") || !delegateExecution.hasVariable("action")) return;
        JSONObject action = new JSONObject(String.valueOf(delegateExecution.getVariable("action")));

        if (!action.getBoolean("changeNumbers") && !action.getBoolean("changeIP") && !action.getBoolean("changeConnection")) return;


        JSONObject techSpecs = new JSONObject(String.valueOf(delegateExecution.getVariable("techSpecs")));
        String clientBIN = delegateExecution.getVariable("clientBIN").toString();
        String fkClient = getPBXFKClientByBIN(clientBIN);

        if (fkClient == null) {
            throw new Exception("No fk_client found for BIN '" + clientBIN + "'.");
        }

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet getRequest = new HttpGet("http://sao.kcell.kz/apis/PbxClientDetail?fk_client=" + fkClient);
        CloseableHttpResponse response = client.execute(getRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Something went wrong while getting data from SAO... msg: '" + response.getStatusLine().getReasonPhrase() + "'.");
        }
        JSONObject clientDetails = new JSONObject(EntityUtils.toString(response.getEntity()));
        JSONObject clientData = clientDetails.getJSONObject("data");

        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("fk_client", fkClient));

        Iterator<String> keys = clientData.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!skippedFields.contains(key.toUpperCase())) {
                params.add(new BasicNameValuePair(key.toLowerCase(), clientData.getString(key)));
            }
        }

        String ips = null, cltNumber = null, voicePlatform = null;
        if (clientData.has("IPS")) ips = clientData.getString("IPS");
        if (clientData.has("CLT_NUMBER")) cltNumber = clientData.getString("CLT_NUMBER");
        if (clientData.has("VOICE_PLATFORM")) voicePlatform = clientData.getString("VOICE_PLATFORM");

        if (action.getBoolean("changeNumbers")) {
            String pbxNumbers = techSpecs.getString("pbxNumbers");
            params.add(new BasicNameValuePair("clt_number", pbxNumbers));

            if (ips != null) params.add(new BasicNameValuePair("ips", ips));
            if (voicePlatform != null) params.add(new BasicNameValuePair("voice_platform", voicePlatform));
        } else if (action.getBoolean("changeIP")) {
            JSONObject sip = new JSONObject(String.valueOf(techSpecs.get("sip")));
            String voiceIP = sip.getString("newPublicVoiceIP");
            String signalingIP = sip.getString("newSignalingIP");
            if (!voiceIP.equals(signalingIP)) voiceIP += "; " + signalingIP;
            params.add(new BasicNameValuePair("ips", voiceIP));

            if (cltNumber != null) params.add(new BasicNameValuePair("clt_number", cltNumber));
            if (voicePlatform != null) params.add(new BasicNameValuePair("voice_platform", voicePlatform));
        } else {
            String connectionPoint = techSpecs.getString("connectionPointNew");
            params.add(new BasicNameValuePair("voice_platform", connectionPoint));

            if (ips != null) params.add(new BasicNameValuePair("ips", ips));
            if (cltNumber != null ) params.add(new BasicNameValuePair("clt_number", cltNumber));
        }
        HttpPost postRequest = new HttpPost("http://sao.kcell.kz/apis/PbxClientUpdate");
        postRequest.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
        client = HttpClients.createDefault();

        response = client.execute(postRequest);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new Exception("Something went wrong while posting data to SAO... msg: '" + response.getStatusLine().getReasonPhrase() + "'.");
        }
    }


    public String getPBXFKClientByBIN(String pbxBIN) {

        if (pbxBIN == null || pbxBIN.length() != 12 ) {
            return null;
        }

        List<HistoricProcessInstance> processes = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("PBX").variableValueEquals("clientBIN", pbxBIN).finished().orderByProcessInstanceEndTime().desc().list();
        HistoricProcessInstance process = null;
        if (!processes.isEmpty()) process = processes.get(0);

        if (process != null && process.getId() != null) {
            HistoricVariableInstance fk_client = historyService.createHistoricVariableInstanceQuery().processInstanceId(process.getId()).variableName("fk_client").singleResult();
            if (fk_client != null) return fk_client.getValue().toString();
        }
        return null;
    }


}
