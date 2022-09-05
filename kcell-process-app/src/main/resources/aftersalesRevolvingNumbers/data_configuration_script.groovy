import com.amazonaws.util.json.Jackson
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.camunda.spin.Spin.*;

def configure(DelegateExecution execution) {

    def action = ((JacksonJsonNode) execution.getVariable("action")).unwrap()
    def techSpecs = ((JacksonJsonNode) execution.getVariable("techSpecs")).unwrap()
    def legalInfo = ((JacksonJsonNode) execution.getVariable("legalInfo")).unwrap()

    techSpecs.get('sip').putAt('connectionPoint', techSpecs.get('connectionPoint'))
    legalInfo.putAt('salesRepr', legalInfo.get('KAE'))

    if (action.get('changeConnection').asBoolean()) {
        techSpecs.get('sip').putAt('connectionPoint', techSpecs.get('connectionPointNew'))
        techSpecs.putAt('connectionPointNew', null)
    }
    if (techSpecs.get('sip') != null) {
        if (action.get('newCallerID').asBoolean() || action.get('changeIP').asBoolean()) {
            techSpecs.get('sip').putAt('curPublicVoiceIP', techSpecs.get('sip').get('newPublicVoiceIP'))
            techSpecs.get('sip').putAt('curSignalingIP', techSpecs.get('sip').get('newSignalingIP'))
        }

        techSpecs.get('sip').putAt('voiceIP', techSpecs.get('sip').get('curPublicVoiceIP'))
        techSpecs.get('sip').putAt('signalingIP', techSpecs.get('sip').get('curSignalingIP'))

        techSpecs.get('sip').putAt('newPublicVoiceIP', null)
        techSpecs.get('sip').putAt('newSignalingIP', null)
    }
    techSpecs.putAt('removalRequired', null)
    techSpecs.putAt('removalNumbers', null)

    execution.setVariable("finalResolution", "Approve")
    execution.setVariable("legalInfo", S(legalInfo.toString()))
    execution.setVariable("techSpecs", S(techSpecs.toString()))
}

configure(execution)

