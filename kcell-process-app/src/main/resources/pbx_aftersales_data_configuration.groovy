import com.amazonaws.util.json.Jackson
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.spin.impl.json.jackson.JacksonJsonNode

def configure(DelegateExecution execution) {

    def action = ((JacksonJsonNode) execution.getVariable("action")).unwrap()
    def techSpecs = ((JacksonJsonNode) execution.getVariable("techSpecs")).unwrap()

    if (action.get('changeConnection').asBoolean()) {
        techSpecs.putAt('connectionPoint', techSpecs.get('connectionPointNew'))
        techSpecs.putAt('connectionPointNew', null)
    }
    if (techSpecs.get('sip') != null) {
        if (action.get('changeConnection').asBoolean() || action.get('changeIP').asBoolean()) {
            techSpecs.get('sip').putAt('curPublicVoiceIP', techSpecs.get('sip').get('newPublicVoiceIP'))
            techSpecs.get('sip').putAt('curSignalingIP', techSpecs.get('sip').get('newSignalingIP'))
        }
        techSpecs.get('sip').putAt('newPublicVoiceIP', null)
        techSpecs.get('sip').putAt('newSignalingIP', null)
        techSpecs.get('sip').putAt('description', null)
    }
    techSpecs.putAt('removalRequired', null)
    techSpecs.putAt('removalNumbers', null)

    if (action.get('agreementTermination').asBoolean()) {
        execution.removeVariable("techSpecs");
    } else {
        execution.setVariable("techSpecs", techSpecs.toString())
    }
    execution.setVariable("finalResolution", "Approve")
}

configure(execution)

