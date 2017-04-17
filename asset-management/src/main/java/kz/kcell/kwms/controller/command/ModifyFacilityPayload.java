package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.FacilityInstance;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by admin on 4/17/17.
 */
public class ModifyFacilityPayload implements Payload {

    public String id;

    public JsonNode params;
    public String definition;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        FacilityInstance facilityInstance = commandController.facilityInstanceRepository.findOne(Long.valueOf(this.id));
        facilityInstance.setDefinition(commandController.facilityDefinitionRepository.findOne(this.definition));
        facilityInstance.setParams(this.params.toString());

        facilityInstance = commandController.facilityInstanceRepository.save(FacilityInstance.builder()
                .definition(commandController.facilityDefinitionRepository.findOne(this.definition))
                .params(this.params.toString())
                .build());
        commandController.facilityInstanceRepository.save(facilityInstance);
    }
}
