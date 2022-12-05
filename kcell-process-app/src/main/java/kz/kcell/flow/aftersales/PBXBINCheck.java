package kz.kcell.flow.aftersales;

import lombok.extern.java.Log;
import lombok.val;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service("PBXBINCheck")
@RequestMapping("/aftersales")
@Log
public class PBXBINCheck {

    private final String baseURL;

    @Autowired
    private IdentityService identityService;

    @Autowired
    public PBXBINCheck (@Value("${mail.message.baseurl:http://localhost}") String baseURL) {
        this.baseURL = baseURL;
    }

    @Autowired
    HistoryService historyService;

    @RequestMapping(value = "/pbx/bin/{pbxBIN}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getPBXDataByBIN(@PathVariable("pbxBIN") String pbxBIN, HttpServletRequest request) throws Exception {

        if (pbxBIN == null || pbxBIN.length() != 12 ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No bin or incorrect bin specified");
        }

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }


        List<HistoricProcessInstance> processes = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("AftersalesPBX").variableValueEquals("clientBIN", pbxBIN).variableValueEquals("finalResolution", "Approve").finished().orderByProcessInstanceEndTime().desc().list();
        HistoricProcessInstance process = null;
        if (!processes.isEmpty()) process = processes.get(0);

        if (process != null && process.getId() != null) {
                List<HistoricVariableInstance> vars = historyService.createHistoricVariableInstanceQuery().processInstanceId(process.getId()).list();

                JSONObject response = new JSONObject();
                response.put("aftersales", true);
                for (HistoricVariableInstance v : vars) {
                    if (v.getName().equals("legalInfo")) response.put("legalInfo", v.getValue());
                    else if (v.getName().equals("techSpecs")) response.put("techSpecs", v.getValue());
                    else if (v.getName().equals("action")) response.put("action", v.getValue());
                    else if (v.getName().equals("rootForCMMBGW")) response.put("rootForCMMBGW", v.getValue());
                    else if (v.getName().equals("attachments")) response.put("attachments", v.getValue());
                }

                return ResponseEntity.ok(response.toString());
        } else {
            processes = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("PBX").variableValueEquals("clientBIN", pbxBIN).finished().orderByProcessInstanceEndTime().desc().list();

            val additionalProcesses = historyService.createHistoricProcessInstanceQuery().processDefinitionKey("PBX")
                .variableValueEquals("clientBIN", pbxBIN)
                .unfinished()
                .variableValueEquals("importDataResolution", "Accepted")
                .orderByProcessInstanceEndTime().desc().list();

            processes.addAll(additionalProcesses);

            process = null;
            if (!processes.isEmpty()) process = processes.get(0);

            if (process != null && process.getId() != null) {
                List<HistoricVariableInstance> vars = historyService.createHistoricVariableInstanceQuery().processInstanceId(process.getId()).list();

                JSONObject response = new JSONObject();
                for (HistoricVariableInstance v : vars) {
                    if (v.getName().equals("customerInformation")) response.put("legalInfo", v.getValue());
                    else if (v.getName().equals("technicalSpecifications")) response.put("techSpecs", v.getValue());
                    else if (v.getName().equals("sipProtocol")) response.put("sip", v.getValue());
                    else if (v.getName().equals("tariff")) response.put("tariff", v.getValue());
                    else if (v.getName().equals("clientPriority")) response.put("clientPriority", v.getValue());
                    else if (v.getName().equals("rootForCMMBGW")) response.put("rootForCMMBGW", v.getValue());
                    else if (v.getName().equals("tariffExtra")) response.put("tariffExtra", v.getValue());
                    else if (v.getName().equals("attachments")) response.put("attachments", v.getValue());
                }

                return ResponseEntity.ok(response.toString());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No PBX process with such BIN found!");
    }
}
