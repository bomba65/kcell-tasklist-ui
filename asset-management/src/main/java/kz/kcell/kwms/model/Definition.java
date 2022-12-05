package kz.kcell.kwms.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kz.kcell.kwms.jackson.JsonAsStringDeserializer;
import org.hibernate.validator.constraints.NotBlank;

public interface Definition {
    @NotBlank
    @JsonRawValue
    @JsonDeserialize(using = JsonAsStringDeserializer.class)
    String getSchema();
}
