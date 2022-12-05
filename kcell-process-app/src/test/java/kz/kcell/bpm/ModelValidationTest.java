package kz.kcell.bpm;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SendTask;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnector;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaConnectorId;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.validation.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class ModelValidationTest {

    @Test
    public void validateBpmnFiles() throws Exception {
        List<ModelElementValidator<?>> validators = getValidators();
        StringWriter stringWriter = new StringWriter();
        Files.find(
                Paths.get("src/main/resources/"),
                5,
                (path, basicFileAttributes) -> basicFileAttributes.isRegularFile() && path.toString().endsWith(".bpmn")
        ).forEach(path -> checkBpmn(validators, path.toFile(), stringWriter));

        Assert.assertTrue(stringWriter.toString(), stringWriter.toString().isEmpty());

    }

    private <T extends ModelElementInstance> ModelElementValidator<T> makeValidator(Class<T> elementClass, BiConsumer<T, ValidationResultCollector> consumer){
        return new ModelElementValidator<T>() {
            @Override
            public Class<T> getElementType() {
                return elementClass;
            }

            @Override
            public void validate(T element, ValidationResultCollector validationResultCollector) {
                consumer.accept(element, validationResultCollector);
            }
        };
    }

    private List<ModelElementValidator<?>> getValidators() {
        return Arrays.asList(makeValidator(UserTask.class, (UserTask userTask, ValidationResultCollector validationResultCollector) -> {
            String formKey = userTask.getCamundaFormKey();
            if (userTask.getId().matches("^Task_[a-z0-9]+$")) {
                validationResultCollector.addError(0, "Don't use generated Task IDs");
            }

            if (null == formKey) {
                validationResultCollector.addError(0, "Form key not specified");
            } else {
                String prefix = "embedded:app:";
                if (!formKey.startsWith(prefix)) {
                    validationResultCollector.addError(0, String.format("Invalid form key: %s", formKey));
                } else {
                    Path formsDir = Paths.get("src/main/resources/static");
                    Path formFile = formsDir.resolve(formKey.substring(prefix.length()));

                    if (!Files.exists(formFile) || !Files.isRegularFile(formFile)) {
                        validationResultCollector.addError(0, String.format("Form key refers to non-existing form file: %s", formFile.toString()));
                    }
                }
            }
        }),makeValidator(SendTask.class, (SendTask sendTask, ValidationResultCollector validationResultCollector) -> {
            boolean hasMailSendConnector = Optional.ofNullable(sendTask.getExtensionElements())
                    .flatMap(extensionElements -> extensionElements
                            .getElementsQuery()
                            .filterByType(CamundaConnector.class)
                            .list()
                            .stream()
                            .map(CamundaConnector::getCamundaConnectorId)
                            .filter(Objects::nonNull)
                            .map(CamundaConnectorId::getTextContent)
                            .filter("mail-send"::equals)
                            .findAny()
                    ).isPresent();

            boolean delegateClassIsSpecifiedAndLoadable = Optional.ofNullable(sendTask.getCamundaClass()).flatMap(className -> {
                try {
                    Class<?> delegateClass = Class.forName(className);
                    return Optional.of(delegateClass);
                } catch (ClassNotFoundException e) {
                    return Optional.empty();
                }
            }).isPresent();

            boolean delegateExpressionIsPresent = Optional.ofNullable(sendTask.getCamundaDelegateExpression()).isPresent();
            boolean typeIsPresent = Optional.ofNullable(sendTask.getCamundaType()).isPresent();
            boolean expressionIsPresent = Optional.ofNullable(sendTask.getCamundaExpression()).isPresent();

            if (Stream.of(hasMailSendConnector,
                    delegateClassIsSpecifiedAndLoadable,
                    delegateExpressionIsPresent,
                    typeIsPresent,
                    expressionIsPresent).allMatch(Boolean.FALSE::equals)) {
                validationResultCollector.addError(0, "must specify task implementation");
            }
        }),makeValidator(Process.class, (Process process, ValidationResultCollector validationResultCollector) -> {
            Optional<String> templateName = Stream.of(process)
                .map(Process::getExtensionElements)
                .filter(Objects::nonNull)
                .flatMap(e -> e.getElementsQuery().filterByType(CamundaProperties.class).list().stream())
                .flatMap(e -> e.getCamundaProperties().stream())
                .filter(e -> e.getCamundaName().equals("taskNotificationTemplate"))
                .map(e -> e.getCamundaValue())
                .findAny();

            if(templateName.isPresent()){
                Path tplDir = Paths.get("src/main/resources/");
                Path tplFile = tplDir.resolve(templateName.get().substring(1));

                if (!Files.exists(tplFile) || !Files.isRegularFile(tplFile)) {
                    validationResultCollector.addError(0, String.format("Custom task notification value refers to non-existing .tpl file: %s", tplFile.toString()));
                }
            }
        }),makeValidator(SendTask.class, (SendTask sendTask, ValidationResultCollector validationResultCollector) -> {
            boolean hasFreeMarker = Optional.ofNullable(sendTask.getExtensionElements())
                .flatMap(scripts -> scripts
                    .getElementsQuery()
                    .filterByType(CamundaConnector.class)
                    .list()
                    .stream()
                    .map(CamundaConnector::getCamundaInputOutput)
                    .filter(Objects::nonNull)
                    .flatMap(e -> e.getCamundaInputParameters().stream())
                    .flatMap(e -> e.getChildElementsByType(CamundaScript.class).stream())
                    .filter(e -> e.getCamundaResource().endsWith(".ftl"))
                    .findAny()
                ).isPresent();

            if (Stream.of(hasFreeMarker).allMatch(Boolean.TRUE::equals)) {
                validationResultCollector.addError(0, " change freemarker template to groovy template");
            }
        }));
    }

    private void checkBpmn(List<ModelElementValidator<?>> validators, File bpmnFile, StringWriter stringWriter ) {
        BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(bpmnFile);
        ValidationResults validationResults = bpmnModelInstance.validate(validators);

        if (validationResults.hasErrors()) {
            stringWriter.write(String.format("Bpmn resource %s has following errors:\n", bpmnFile));
            validationResults.write(stringWriter, new ValidationResultFormatter() {
                @Override
                public void formatElement(StringWriter writer, ModelElementInstance element) {
                }

                @Override
                public void formatResult(StringWriter writer, ValidationResult result) {
                    writer.write(String.format(" - %s( %s ): %s\n",
                            result.getElement().getDomElement().getLocalName(),
                            result.getElement().getDomElement().getAttribute("id"),
                            result.getMessage()
                    ));
                }
            });
            stringWriter.write("\n");
        }

    }
}
