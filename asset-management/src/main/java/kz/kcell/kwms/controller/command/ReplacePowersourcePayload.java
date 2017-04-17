package kz.kcell.kwms.controller.command;

import com.fasterxml.jackson.databind.JsonNode;
import kz.kcell.kwms.model.PowerSource;
import kz.kcell.kwms.model.Site;

import java.util.Map;

public class ReplacePowersourcePayload implements Payload {
    public String id;
    public JsonNode params;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        commandController.powerSourceRepository.delete(Long.valueOf(id));

        commandController.powerSourceRepository.save(PowerSource.builder()
                .params(this.params.toString())
                .site((Site) objectMap.get("site"))
                .build());
    }
}
