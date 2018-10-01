package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
//import kz.kcell.kwms.model.Location;

import java.util.Map;

public class CreateLocationPayload implements Payload {

    public String name;
    public String sitename;
    public JsonNode params;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
    }
}
