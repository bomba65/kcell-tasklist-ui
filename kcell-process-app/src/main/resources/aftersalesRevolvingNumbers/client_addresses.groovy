import org.camunda.bpm.engine.delegate.DelegateExecution
import java.util.stream.Collectors

def email = techSpecs.unwrap().get('contactEmail').asText()

email
