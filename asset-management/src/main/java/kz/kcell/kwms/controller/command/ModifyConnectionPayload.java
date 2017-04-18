package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.ConnectionInstance;
import kz.kcell.kwms.model.EquipmentInstance;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Created by admin on 4/17/17.
 */
public class ModifyConnectionPayload implements Payload {


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

        ConnectionInstance connectionInstance;
        if (this.id != null && !this.id.isEmpty()) {
            connectionInstance = commandController.connectionInstanceRepository.findOne(Long.valueOf(this.id));
            connectionInstance.setDefinition(commandController.connectionDefinitionRepository.findOne(this.definition));
            connectionInstance.setParams(this.params.toString());
            connectionInstance.setEquipments(equipmentsField);
            commandController.connectionInstanceRepository.save(connectionInstance);
        }
    }
}
