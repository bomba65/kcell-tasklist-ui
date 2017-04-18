package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kz.kcell.kwms.model.EquipmentInstance;
import kz.kcell.kwms.model.FacilityInstance;
import kz.kcell.kwms.model.InstallationInstance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 4/17/17.
 */
public class ReplaceEquipmentPayload implements Payload {

    public String facility;
    public String definition;
    public JsonNode params;
    public String id;
    public CreateEquipmentPayload equipment;

    @Override
    public Set<String> getDependencies() {
        Set<String> result = new HashSet<>();
        if (facility != null && facility.startsWith("_NEW:")) {
            result.add(facility);
        }
        result.addAll(equipment.getDependencies());
        return result;
    }

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        equipment.execute(tCommand, commandController, objectMap);

        FacilityInstance facilityInstance = null;
        if (this.facility != null) {
            if (this.facility.startsWith("_NEW:")) {
                facilityInstance = (FacilityInstance) objectMap.get(this.facility);
            } else {
                facilityInstance = commandController.facilityInstanceRepository.findOne(Long.valueOf(this.facility));
            }
        }
        InstallationInstance installationInstance = commandController.installationInstanceRepository.findOne(Long.valueOf(this.id));
        installationInstance.setDefinition(commandController.installationDefinitionRepository.findOne(this.definition));
        installationInstance.setFacility(facilityInstance);
        installationInstance.setParams(this.params != null ? this.params.toString() : "{}");
        installationInstance.setEquipment((EquipmentInstance) objectMap.get(equipment.id));
        commandController.installationInstanceRepository.save(installationInstance);
    }
}
