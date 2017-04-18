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

public class InstallNewEquipmentPayload implements Payload {

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

        InstallationInstance installationInstance = commandController.installationInstanceRepository.save(InstallationInstance.builder()
                .definition(commandController.installationDefinitionRepository.findOne(this.definition))
                .equipment((EquipmentInstance) objectMap.get(equipment.id))
                .facility(facilityInstance)
                .params(this.params != null ? this.params.toString() : "{}")
                .site((Site) objectMap.get("site"))
                .build());
        objectMap.put(this.id, installationInstance);
    }
}
