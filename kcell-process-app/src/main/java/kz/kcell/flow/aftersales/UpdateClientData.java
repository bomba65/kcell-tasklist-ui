package kz.kcell.flow.aftersales;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("AftersalesUpdateClientData")
@lombok.extern.java.Log

public class UpdateClientData implements JavaDelegate {

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
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("fk_client", fkClient));

        if (action.getBoolean("changeNumbers")) {
            String pbxNumbers = techSpecs.getString("pbxNumbers");
            params.add(new BasicNameValuePair("clt_number", pbxNumbers));
        } else if (action.getBoolean("changeIP")) {
            JSONObject sip = new JSONObject(String.valueOf(techSpecs.get("sip")));
            String voiceIP = sip.getString("newPublicVoiceIP");
            String signalingIP = sip.getString("newSignalingIP");
            if (!voiceIP.equals(signalingIP)) voiceIP += "; " + signalingIP;
            params.add(new BasicNameValuePair("ips", voiceIP));
        } else {
            String connectionPoint = techSpecs.getString("connectionPointNew");
            params.add(new BasicNameValuePair("voice_platform", connectionPoint));
        }
        HttpPut request = new HttpPut("http://sao.kcell.kz/apis/PbxClientUpdate?fk_client=" + fkClient);
        request.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
        CloseableHttpClient client = HttpClients.createDefault();
        client.execute(request);

        //CloseableHttpResponse response = client.execute(request);
        //response.getStatusLine().getStatusCode() == 200?
    }


    public String getPBXFKClientByBIN(String pbxBIN) {

        if (pbxBIN == null || pbxBIN.length() != 12 ) {
            return null;
        }

        List<HistoricProcessInstance> processes = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("PBX").variableValueEquals("clientBIN", pbxBIN).finished().orderByProcessInstanceEndTime().desc().desc().list();
        HistoricProcessInstance process = null;
        if (!processes.isEmpty()) process = processes.get(0);

        if (process != null && process.getId() != null) {
            HistoricVariableInstance fk_client = historyService.createHistoricVariableInstanceQuery().processInstanceId(process.getId()).variableName("fk_client").singleResult();
            if (fk_client != null) return fk_client.getValue().toString();
        }
        return null;
    }


}
