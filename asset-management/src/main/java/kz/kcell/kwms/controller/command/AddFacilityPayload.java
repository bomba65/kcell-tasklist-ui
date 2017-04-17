package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.FacilityInstance;
import kz.kcell.kwms.model.Site;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class AddFacilityPayload implements Payload {

    public String id;
    public JsonNode params;
    public String definition;

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
