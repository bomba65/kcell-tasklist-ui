package kz.kcell.flow.leasing;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.script.*;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/leasing")
@Log
public class LeasingController {

    private final CompiledScript template;

    @Autowired
    private IdentityService identityService;

    @Autowired
    RuntimeService runtimeService;


    @Autowired
    public LeasingController(ScriptEngineManager manager) throws ScriptException {
        ScriptEngine groovy = manager.getEngineByName("groovy");
        InputStreamReader reader = new InputStreamReader(LeasingController.class.getResourceAsStream("/template/leasing/rent_req.groovy"));
        this.template = ((Compilable)groovy).compile(reader);
    }


    @RequestMapping(value = "/rrfile/create", method = RequestMethod.POST, produces={"application/json"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> FreephoneClientCreateUpdate(@RequestBody String saoRequestBody) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        try {
            Bindings bindings = template.getEngine().createBindings();
//            bindings.put("delegateTask", delegateTask);
            String actOfAcceptanceHtml = String.valueOf(template.eval(bindings));

            FileValue actOfAcceptanceDocument = null;
            actOfAcceptanceDocument = Variables.fileValue("actOfAcceptance.doc").file(actOfAcceptanceHtml.getBytes("utf-8")).mimeType("application/msword").create();
        } catch (ScriptException e) {
            throw new RuntimeException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }


        return null;
    }
}
