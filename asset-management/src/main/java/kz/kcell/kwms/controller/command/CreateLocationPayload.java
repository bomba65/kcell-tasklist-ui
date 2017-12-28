package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
//import kz.kcell.kwms.model.Location;

import java.util.Map;

public class CreateLocationPayload implements Payload {

    public String name;
    public Long siteId;
    public JsonNode params;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
/*        commandController.locationRepository.save(Location.builder()
                .name(this.name)
                .site(commandController.siteRepository.findOne(this.siteId))
                .params(this.params != null ? this.params.toString() : "{}")
                .build());
*/    }
}
