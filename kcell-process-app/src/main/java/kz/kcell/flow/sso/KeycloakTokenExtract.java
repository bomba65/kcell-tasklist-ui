package kz.kcell.flow.sso;

import lombok.extern.java.Log;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Log
@RestController
@RequestMapping("/keycloak")
public class KeycloakTokenExtract {

    @Autowired
    private IdentityService identityService;

    @RequestMapping(value = "/token/extract", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> technicalCondition(HttpServletResponse response) throws Exception {

        return ResponseEntity.ok(identityService.getCurrentAuthentication().getUserId());
    }
}
