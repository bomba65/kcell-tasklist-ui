package kz.kcell.kwms.controller.command;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Command<T extends Payload> {

    public T payload;
    public CommandName command;

    public void execute(CommandController commandController, Map<String, Object> objectMap) {
        payload.execute(this, commandController, objectMap);
    }

    public Set<String> getDependencies() {
        if (payload != null) {
            return payload.getDependencies();
        } else {
            return Collections.emptySet();
        }
    }

}
