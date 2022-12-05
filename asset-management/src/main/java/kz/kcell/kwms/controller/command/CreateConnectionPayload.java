package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.ConnectionInstance;
import kz.kcell.kwms.model.EquipmentInstance;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@ToString
public class CreateConnectionPayload implements Payload {

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
                        .params(this.params != null ? this.params.toString() : "{}")
                        .equipments(equipmentsField)
                        .build());
        objectMap.put(this.id, connectionInstance);
    }
}
