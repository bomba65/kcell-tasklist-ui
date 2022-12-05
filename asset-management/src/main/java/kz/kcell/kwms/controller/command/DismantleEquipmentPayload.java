package kz.kcell.kwms.controller.command;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class DismantleEquipmentPayload implements Payload {

    public String id;

    @Override
    public <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap) {
        commandController.installationInstanceRepository.delete(Long.valueOf(this.id));
    }
}
