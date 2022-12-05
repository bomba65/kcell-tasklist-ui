package revision

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.identity.Authentication

import java.util.function.Supplier
import java.util.stream.Collectors

def getUserEmail(DelegateExecution execution) {
    String siteRegion = execution.getVariable("siteRegion").toString()
    def result
    switch (siteRegion) {
        case 'alm':
            result = 'Andrey.Ivanov@kcell.kz'
            break;
        case 'astana':
            result = 'Marat.Abdin@kcell.kz'
            break;
        case 'west':
            result = 'Nikolay.Ustinov@kcell.kz'
            break;
        case 'nc':
            result = 'Alexandr.Galat@kcell.kz'
            break;
        case 'south':
            result = 'Sheraly.Tolymbekov@kcell.kz'
            break;
    }
    result
}

getUserEmail(execution)
