package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.PowerSource;

import java.util.Map;

/**
 * Created by admin on 4/17/17.
 */
public class ModifyPowersourcePayload implements Payload {

    public String id;
    public JsonNode params;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        PowerSource powerSource = commandController.powerSourceRepository.findOne(Long.valueOf(this.id));
        powerSource.setParams(params.toString());
        commandController.powerSourceRepository.save(powerSource);
    }
}
