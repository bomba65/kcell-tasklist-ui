package kz.kcell.kwms.controller.command;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class RemovePowersourcePayload implements Payload {
    public String id;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        commandController.powerSourceRepository.delete(Long.valueOf(this.id));
    }
}
