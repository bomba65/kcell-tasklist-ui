package kz.kcell.kwms.controller;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.repository.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log
public class CommandController {
    private final SiteRepository siteRepository;
    private final FacilityInstanceRepository facilityInstanceRepository;
    private final InstallationInstanceRepository installationInstanceRepository;
    private final EquipmentInstanceRepository equipmentInstanceRepository;
    private final PowerSourceRepository powerSourceRepository;

    @Autowired
    public CommandController(
            SiteRepository sr,
            FacilityInstanceRepository fir,
            InstallationInstanceRepository iir,
            EquipmentInstanceRepository eir,
            PowerSourceRepository psr) {
        this.siteRepository = sr;
        this.facilityInstanceRepository = fir;
        this.installationInstanceRepository = iir;
        this.equipmentInstanceRepository = eir;
        this.powerSourceRepository = psr;
    }

    @PostMapping(path = "/command", consumes = "application/json")
    @Transactional
    public void processCommands(@RequestBody JsonNode commands) {
        log.info(commands.toString());

    }
}
