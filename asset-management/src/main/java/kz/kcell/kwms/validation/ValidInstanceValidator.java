package kz.kcell.kwms.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import kz.kcell.kwms.model.Instance;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidInstanceValidator implements ConstraintValidator<ValidInstance, Instance> {
    ObjectMapper mapper;
    JsonSchemaFactory factory;

    @Autowired
    public ValidInstanceValidator(ObjectMapper mapper, JsonSchemaFactory factory) {
        this.mapper = mapper;
        this.factory = factory;
    }

    @Override
    public void initialize(ValidInstance constraintAnnotation) {

    }

    @Override
    public boolean isValid(Instance instance, ConstraintValidatorContext context) {

        // use @NotNull to validate these
        if (instance.getParams() == null
           || instance.getDefinition() == null
           || instance.getDefinition().getSchema() == null) return true;

        String schema = instance.getDefinition().getSchema();
        String params = instance.getParams();

        // use @NotBlank to validate these
        if (schema.trim().isEmpty() || params.trim().isEmpty()) return true;

        try {
            JsonNode schemaNode = mapper.readTree(schema);

            JsonSchema jsonSchema = factory.getJsonSchema(schemaNode);

            JsonNode paramsNode = mapper.readTree(params);
            ProcessingReport report = jsonSchema.validate(paramsNode);

            return report.isSuccess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
