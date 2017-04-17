package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.EquipmentInstance;
import kz.kcell.kwms.model.PowerSource;
import kz.kcell.kwms.model.Site;

import java.util.Map;

/**
 * Created by admin on 4/17/17.
 */
public class CreatePowersourcePayload implements Payload {

    public JsonNode params;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        commandController.powerSourceRepository.save(PowerSource.builder()
                .params(this.params.toString())
                .site((Site) objectMap.get("site"))
                .build());
    }
}
