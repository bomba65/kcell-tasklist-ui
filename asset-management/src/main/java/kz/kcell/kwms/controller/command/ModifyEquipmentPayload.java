package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.EquipmentInstance;

import java.util.Map;

public class ModifyEquipmentPayload implements Payload {
    public String id;
    public String definition;
    public JsonNode params;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        EquipmentInstance equipmentInstance = commandController.equipmentInstanceRepository.findOne(Long.valueOf(this.id));
        equipmentInstance.setDefinition(commandController.equipmentDefinitionRepository.findOne(this.definition));
        equipmentInstance.setParams(this.params.toString());
        commandController.equipmentInstanceRepository.save(equipmentInstance);
    }
}
