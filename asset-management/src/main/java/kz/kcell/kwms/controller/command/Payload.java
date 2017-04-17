package kz.kcell.kwms.controller.command;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public interface Payload {

    default Set<String> getDependencies(){
        return Collections.emptySet();
    }

    <T extends Payload> void execute(Command tCommand, CommandController commandController, Map<String, Object> objectMap);
}
