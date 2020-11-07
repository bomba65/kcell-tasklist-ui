
package kz.kcell.flow.revision;

import kz.kcell.flow.repository.custom.ReportRepository;
import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/jrblankfix")
@Log
public class JrBlankFixController {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    JrBlankFixService jrBlankFixService;

    @Value("${mail.message.baseurl:http://localhost}")
    String baseUri;

    @RequestMapping(value = "/execute", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> execute() throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        List<String> keys = new ArrayList<>();
        keys.add("W-");
        keys.add("S-");
        keys.add("E-");
        keys.add("Ast-");

        for (String key : keys) {
            List<ProcessInstance> list = runtimeService.createProcessInstanceQuery().processDefinitionKey("Revision").processInstanceBusinessKeyLike(key + '%').list();
            log.info("Checking " + key + " list size: " + list.size());

            for (ProcessInstance p : list) {
               SpinJsonNode jrBlank = runtimeService.<JsonValue>getVariableTyped(p.getRootProcessInstanceId(),"jrBlank").getValue();
               String name = jrBlank.prop("name").stringValue();
               if(!p.getBusinessKey().contains("##") && !p.getBusinessKey().equals(name.replace(".xlsx", ""))) {
                   jrBlankFixService.execute(p.getProcessInstanceId());

               }
            }
        }

        return ResponseEntity.ok("ok");
    }
}
