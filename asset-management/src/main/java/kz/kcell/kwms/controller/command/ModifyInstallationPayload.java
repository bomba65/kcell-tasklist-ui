package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.kcell.kwms.model.EquipmentInstance;
import kz.kcell.kwms.model.FacilityInstance;
import kz.kcell.kwms.model.InstallationInstance;
import kz.kcell.kwms.model.Site;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 4/17/17.
 */
public class ModifyInstallationPayload implements Payload {

    public String id;
    public JsonNode params;
    public ModifyEquipmentPayload equipment;
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
        InstallationInstance installationInstance = commandController.installationInstanceRepository.findOne(Long.valueOf(this.id));
        installationInstance.setDefinition(commandController.installationDefinitionRepository.findOne(this.definition));
        installationInstance.setFacility(facilityInstance);
        installationInstance.setParams(params.toString());
        commandController.installationInstanceRepository.save(installationInstance);
    }
}
