package kz.kcell.flow.leasing;

import kz.kcell.flow.files.Minio;
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
import java.io.ByteArrayInputStream;
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

    private Minio minioClient;

    @Autowired
    public LeasingController(ScriptEngineManager manager, Minio minioClient) throws ScriptException {
        ScriptEngine groovy = manager.getEngineByName("groovy");
        InputStreamReader reader = new InputStreamReader(LeasingController.class.getResourceAsStream("/template/leasing/rent_req.groovy"));
        this.template = ((Compilable)groovy).compile(reader);

        this.minioClient = minioClient;
    }


    @RequestMapping(value = "/rrfile/create", method = RequestMethod.POST, produces={"application/json"}, consumes="application/json")
    @ResponseBody
    public ResponseEntity<String> FreephoneClientCreateUpdate(@RequestBody RRFileData rrFileData) throws Exception {

        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warning("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No user logged in");
        }

        try {
            Bindings bindings = template.getEngine().createBindings();

            String rrFileHtml = String.valueOf(template.eval(bindings));
            ByteArrayInputStream is = new ByteArrayInputStream(rrFileHtml.getBytes("utf-8"));

            minioClient.saveFile(rrFileData.processInstanceId + "/" + rrFileData.taskId + "/rrFile.docx" , is, "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            System.out.println(rrFileData.processInstanceId + "/" + rrFileData.taskId + "/rrFile.docx");

        } catch (ScriptException e) {
            throw new RuntimeException(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e.getMessage());
        }


        return null;
    }
}
