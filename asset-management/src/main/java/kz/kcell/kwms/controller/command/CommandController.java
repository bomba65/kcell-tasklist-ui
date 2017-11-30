package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import kz.kcell.kwms.repository.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static kz.kcell.kwms.controller.command.CommandName.valueOf;

@RestController
@Log
public class CommandController {

    @Autowired
    SiteRepository siteRepository;
    @Autowired
    FacilityInstanceRepository facilityInstanceRepository;
    @Autowired
    FacilityDefinitionRepository facilityDefinitionRepository;
    @Autowired
    InstallationInstanceRepository installationInstanceRepository;
    @Autowired
    InstallationDefinitionRepository installationDefinitionRepository;
    @Autowired
    EquipmentInstanceRepository equipmentInstanceRepository;
    @Autowired
    EquipmentDefinitionRepository equipmentDefinitionRepository;
    @Autowired
    PowerSourceRepository powerSourceRepository;
    @Autowired
    ConnectionInstanceRepository connectionInstanceRepository;
    @Autowired
    ConnectionDefinitionRepository connectionDefinitionRepository;

    @PostMapping(path = "/command/{siteId}", consumes = "application/json")
    @Transactional
    public void processCommands(@RequestBody JsonNode commands, @PathVariable("siteId") String siteId) throws JsonProcessingException {
        log.info(commands.toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Set<Command<? extends Payload>> commandsList = new HashSet<>();

        for (JsonNode commandNode : commands) {
            Command<Payload> command = new Command<>();
            log.warning(commandNode.toString());
            command.command = valueOf(commandNode.get("command").asText());
            System.out.println("COMMAND:" + command.command);
            switch (command.command) {
                case ADD_FACILITY:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), AddFacilityPayload.class);
                    break;
                case INSTALL_NEW_EQUIPMENT:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), InstallNewEquipmentPayload.class);
                    break;
                case CREATE_POWERSOURCE:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), CreatePowersourcePayload.class);
                    break;
                case CONNECT_EQUIPMENT:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), CreateConnectionPayload.class);
                    break;
                case DISMANTLE_EQUIPMENT:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), DismantleEquipmentPayload.class);
                    break;
                case REMOVE_POWERSOURCE:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), RemovePowersourcePayload.class);
                    break;
                case REMOVE_CONNECTION:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), RemoveConnectionPayload.class);
                    break;
                case MODIFY_FACILITY:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), ModifyFacilityPayload.class);
                    break;
                case MODIFY_INSTALLATION:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), ModifyInstallationPayload.class);
                    break;
                case MODIFY_CONNECTION:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), ModifyConnectionPayload.class);
                    break;
                case MODIFY_POWERSOURCE:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), ModifyPowersourcePayload.class);
                    break;
                case REPLACE_EQUIPMENT:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), ReplaceEquipmentPayload.class);
                    break;
                case REPLACE_POWERSOURCE:
                    command.payload = mapper.treeToValue(commandNode.get("payload"), ReplacePowersourcePayload.class);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown command: " + command.command);
            }
            commandsList.add(command);
        }
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("site", siteRepository.findOne(Long.valueOf(siteId)));

        while (!commandsList.isEmpty()) {
            List<Command<? extends Payload>> independentCommands = commandsList.stream().filter(c -> objectMap.keySet().containsAll(c.getDependencies())).collect(Collectors.toList());
            if (independentCommands.isEmpty() && !commandsList.isEmpty()) {
                throw new IllegalStateException("Could not find independent commands for execution");
            }
            independentCommands.forEach((c) -> {
                c.execute(this, objectMap);
            });
            commandsList.removeAll(independentCommands);
        }
    }
}
