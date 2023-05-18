package kz.kcell.bpm.controller;

import kz.kcell.bpm.service.CamundaVpnPortProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.IdentityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/camunda-process")
public class CamundaProcessController {

    private final IdentityService identityService;
    private final CamundaVpnPortProcessService camundaVpnPortProcessService;

    @GetMapping("/vpn-port-process")
    public ResponseEntity<List<Map<String, String>>> getVpnPortProcess(
        @RequestParam(name = "process", required = false) String process,
        @RequestParam(name = "channel", required = false) String channel,
        @RequestParam(name = "requestType", required = false) String requestType,
        @RequestParam(name = "currentActivity", required = false) String currentActivity,
        @RequestParam(name = "priority", required = false) String priority,
        @RequestParam(name = "dateCreated", required = false) String dateCreated,
        @RequestParam(name = "requestNumber", required = false) String requestNumber,
        @RequestParam(name = "portId", required = false) String portId,
        @RequestParam(name = "vpnId", required = false) String vpnId
    ) throws ParseException {
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warn("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok().body(camundaVpnPortProcessService.getVpnPortProcess(
            process,
            channel,
            requestType,
            currentActivity,
            priority,
            dateCreated,
            requestNumber,
            portId,
            vpnId
        ));
    }

    @GetMapping("vpn-port-process/task-list")
    public ResponseEntity<Map<String, String>> getTaskList() {
        if (identityService.getCurrentAuthentication() == null || identityService.getCurrentAuthentication().getUserId() == null) {
            log.warn("No user logged in");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().body(camundaVpnPortProcessService.getTaskList());
    }
}
