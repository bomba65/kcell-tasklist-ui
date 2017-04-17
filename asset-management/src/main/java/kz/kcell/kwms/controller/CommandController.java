package kz.kcell.kwms.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.kcell.kwms.model.*;
import kz.kcell.kwms.repository.*;
import lombok.ToString;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@Log
public class CommandController {

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private FacilityInstanceRepository facilityInstanceRepository;
    @Autowired
    private FacilityDefinitionRepository facilityDefinitionRepository;
    @Autowired
    private InstallationInstanceRepository installationInstanceRepository;
    @Autowired
    private InstallationDefinitionRepository installationDefinitionRepository;
    @Autowired
    private EquipmentInstanceRepository equipmentInstanceRepository;
    @Autowired
    private EquipmentDefinitionRepository equipmentDefinitionRepository;
    @Autowired
    private PowerSourceRepository powerSourceRepository;
    @Autowired
    private ConnectionInstanceRepository connectionInstanceRepository;
    @Autowired
    private ConnectionDefinitionRepository connectionDefinitionRepository;
//
//    @Autowired
//    public CommandController(
//            SiteRepository sr,
//            FacilityInstanceRepository fir,
//            InstallationInstanceRepository iir,
//            EquipmentInstanceRepository eir,
//            PowerSourceRepository psr) {
//        this.siteRepository = sr;
//        this.facilityInstanceRepository = fir;
//        this.installationInstanceRepository = iir;
//        this.equipmentInstanceRepository = eir;
//        this.powerSourceRepository = psr;
//    }

    @PostMapping(path = "/command/{siteId}", consumes = "application/json")
    @Transactional
    public void processCommands(@RequestBody JsonNode commands, @PathVariable("siteId") String siteId) throws JsonProcessingException {
        log.info(commands.toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Set<Command> commandsList = new HashSet<>();

        for (JsonNode commandNode : commands) {
            Command<Payload> command = new Command<>();
            command.command = commandNode.get("command").asText();
            switch (command.command) {
                case "CREATE_FACILITY":
                    command.payload = mapper.treeToValue(commandNode.get("payload"), CreateFacilityPayload.class);
                    break;
                case "CREATE_INSTALLATION_AND_EQUIPMENT":
                    command.payload = mapper.treeToValue(commandNode.get("payload"), CreateInstallationAndEquipmentPayload.class);
                    break;
                case "CREATE_CONNECTION":
                    command.payload = mapper.treeToValue(commandNode.get("payload"), CreateConnectionPayload.class);
                    break;
            }
            commandsList.add(command);
        }
        Map<String, Object> objectMap = new HashMap<String, Object>();
        objectMap.put("site", siteRepository.findOne(siteId));

        while (!commandsList.isEmpty()) {
            List<Command> independentCommands = commandsList.stream().filter(c -> objectMap.keySet().containsAll(c.getDependencies())).collect(Collectors.toList());
            if (independentCommands.isEmpty() && !commandsList.isEmpty()) {
                throw new IllegalStateException("Could not find independent commands for execution");
            }
            independentCommands.forEach(c -> {
                c.execute(this, objectMap);
            });
            commandsList.removeAll(independentCommands);
        }
    }

    public static class Command<T extends Payload> {

        public T payload;
        public String command;

        public void execute(CommandController commandController, Map<String, Object> objectMap) {
            payload.execute(this, commandController, objectMap);
        }

        public Set<String> getDependencies() {
            if (payload != null) {
                return payload.getDependencies();
            } else {
                return Collections.emptySet();
            }
        }
    }

    public interface Payload {

        Set<String> getDependencies();

        <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap);
    }

    @ToString
    public static class CreateConnectionPayload implements Payload {

        public String id;
        public List<String> equipments;
        public JsonNode params;
        public String definition;

        @Override
        public Set<String> getDependencies() {
            return equipments.stream().filter(e -> e.startsWith("_NEW:")).collect(Collectors.toSet());
        }

        @Override
        public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
            TreeSet<EquipmentInstance> equipmentsField = equipments.stream().map(id -> {
                if (id.startsWith("_NEW:")) {
                    return (EquipmentInstance) objectMap.get(id);
                } else {
                    return commandController.equipmentInstanceRepository.findOne(Long.valueOf(id));
                }
            }).collect(Collectors.toCollection(TreeSet::new));
            ConnectionInstance connectionInstance = commandController.connectionInstanceRepository.save(
                    ConnectionInstance.builder()
                            .definition(commandController.connectionDefinitionRepository.findOne(this.definition))
                            .params(this.params.toString())
                            .equipments(equipmentsField)
                            .build());
            objectMap.put(this.id, connectionInstance);
        }
    }

    public static class CreateFacilityPayload implements Payload {

        public String id;
        public JsonNode params;
        public String definition;

        @Override
        public Set<String> getDependencies() {
            return Collections.emptySet();
        }

        @Override
        public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
            FacilityInstance facilityInstance = commandController.facilityInstanceRepository.save(FacilityInstance.builder()
                    .definition(commandController.facilityDefinitionRepository.findOne(this.definition))
                    .params(this.params.toString())
                    .build());
            objectMap.put(this.id, facilityInstance);
            Site site = (Site) objectMap.get("site");
            site.getFacilities().add(facilityInstance);
            site = commandController.siteRepository.save(site);
            objectMap.put("site", site);
        }
    }

    public static class CreateEquipmentPayload implements Payload {
        public String id;
        public String definition;
        public JsonNode params;

        @Override
        public Set<String> getDependencies() {
            return Collections.emptySet();
        }

        @Override
        public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
            EquipmentInstance equipmentInstance = commandController.equipmentInstanceRepository.save(EquipmentInstance.builder()
                    .definition(commandController.equipmentDefinitionRepository.findOne(this.definition))
                    .params(this.params.toString())
                    .build());
            objectMap.put(this.id, equipmentInstance);
        }
    }

    public static class CreateInstallationAndEquipmentPayload implements Payload {

        public String id;
        public JsonNode params;
        public CreateEquipmentPayload equipment;
        public String facility;
        public String definition;

        @Override
        public Set<String> getDependencies() {
            Set<String> result = new HashSet<>();
            if (facility != null && facility.startsWith("_NEW:")) {
                result.add(facility);
            }
            if (Arrays.asList("RU", "DU", "ANTENNA").contains(definition)) {
                JsonNode rbsNumber = params.get("rbs_number");
                if (rbsNumber != null && rbsNumber.asText().startsWith("_NEW:")) {
                    result.add(rbsNumber.asText());
                }
            }
            result.addAll(equipment.getDependencies());
            return result;
        }

        @Override
        public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
            this.equipment.execute(tCommand, commandController, objectMap);
            FacilityInstance facilityInstance = null;
            if (this.facility != null) {
                if (this.facility.startsWith("_NEW:")) {
                    facilityInstance = (FacilityInstance) objectMap.get(this.facility);
                } else {
                    facilityInstance = commandController.facilityInstanceRepository.findOne(Long.valueOf(this.facility));
                }
            }

            //Replace RBS_NUMBER with Equipment Id
            ObjectNode params = (ObjectNode) this.params;
            if (params.get("rbs_number") != null && params.get("rbs_number").textValue().startsWith("_NEW:")) {
                EquipmentInstance rbs = (EquipmentInstance) objectMap.get(params.get("rbs_number").textValue());
                params.put("rbs_number", rbs.getId());
            }

            InstallationInstance installationInstance = commandController.installationInstanceRepository.save(InstallationInstance.builder()
                    .definition(commandController.installationDefinitionRepository.findOne(this.definition))
                    .equipment((EquipmentInstance) objectMap.get(equipment.id))
                    .facility(facilityInstance)
                    .params(params.toString())
                    .site((Site) objectMap.get("site"))
                    .build());
            objectMap.put(this.id, installationInstance);
        }
    }

    public void createConnection(JsonNode connection) throws JsonProcessingException {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
//        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        CreateConnectionPayload createConnectionPayload = mapper.treeToValue(connection, CreateConnectionPayload.class);
//        ConnectionDefinition connectionDefinition = connectionDefinitionRepository.findOne(createConnectionPayload.definition);
//        Iterable<EquipmentInstance> equipmentInstances = equipmentInstanceRepository.findAll(createConnectionPayload.equipments);
//        System.out.println("START");
//        System.out.println(connectionDefinition);
//        if (connectionDefinition != null) {
//            ConnectionInstance connectionInstance = ConnectionInstance.builder()
//                    .definition(connectionDefinition)
//                    .equipments(StreamSupport.stream(equipmentInstances.spliterator(), false).collect(Collectors.toCollection(TreeSet::new)))
//                    .params(createConnectionPayload.params.toString())
//                    .build();
//            connectionInstance = connectionInstanceRepository.save(connectionInstance);
//            System.out.println(connectionInstance);
//        }
    }
}
