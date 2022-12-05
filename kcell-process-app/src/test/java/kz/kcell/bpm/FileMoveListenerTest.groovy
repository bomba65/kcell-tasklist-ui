package kz.kcell.bpm

import groovy.json.JsonOutput
import kz.kcell.flow.files.FileMoveListener
import kz.kcell.flow.files.Minio
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.variable.Variables
import org.camunda.spin.plugin.variable.SpinValues
import org.junit.Rule
import org.junit.Test
import org.skyscreamer.jsonassert.JSONAssert

import static org.mockito.Mockito.*

@Deployment(resources = 'file-test.bpmn')
class FileMoveListenerTest {
    @Rule
    public ProcessEngineRule processEngineRule = new ProcessEngineRule();

    def J(any) {
        return SpinValues.jsonValue(JsonOutput.toJson(any));
    }

    @Test
    void test() {
        Minio minio = mock(Minio.class);

        Mocks.register("fileMoveListener", new FileMoveListener(minio));

        ProcessInstanceWithVariables exampleProcess = processEngineRule.getRuntimeService()
                .createProcessInstanceByKey("fileTest")
                .setVariables(Variables.createVariables()
                    .putValue("as", J([[
                                               path: 'somepath',
                                               name: 'somename'
                                       ]]))
                    .putValue("bs", J([[
                                               path: 'otherpath',
                                               name: 'othername'
                                       ]]))
                    .putValue("cs", J([[
                                               path: 'morepath',
                                               name: 'morename'
                                       ]]))
                )
                .executeWithVariablesInReturn();

        def processId = exampleProcess.getProcessInstanceId()
        verify(minio, times(1)).moveToPermanentStorage("somepath", "$processId/somename");
        verify(minio, times(1)).moveToPermanentStorage("otherpath", "$processId/othername");
        verify(minio, never()).moveToPermanentStorage("morepath", "$processId/morename");

        JSONAssert.assertEquals(
                exampleProcess.variables.get("as").toString(),
                JsonOutput.toJson([[name: 'somename', path: "$processId/somename"]]),
                false
        )

        JSONAssert.assertEquals(
                exampleProcess.variables.get("bs").toString(),
                JsonOutput.toJson([[name: 'othername', path: "$processId/othername"]]),
                false
        )

        JSONAssert.assertEquals(
                exampleProcess.variables.get("cs").toString(),
                JsonOutput.toJson([[name: 'morename', path: "morepath"]]),
                false
        )
    }
}
