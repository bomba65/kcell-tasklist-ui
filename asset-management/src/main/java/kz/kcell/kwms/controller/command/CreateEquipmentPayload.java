package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.EquipmentInstance;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class CreateEquipmentPayload implements Payload {
    public String id;
    public String definition;
    public JsonNode params;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        EquipmentInstance equipmentInstance = commandController.equipmentInstanceRepository.save(EquipmentInstance.builder()
                .definition(commandController.equipmentDefinitionRepository.findOne(this.definition))
                .params(this.params.toString())
                .build());
        objectMap.put(this.id, equipmentInstance);
    }
}
