package kz.kcell.kwms.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kz.kcell.kwms.jackson.JsonAsStringDeserializer;
import kz.kcell.kwms.validation.ValidInstance;
import lombok.NonNull;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.Valid;

@ValidInstance
public interface Instance<T extends Definition> {
    @Valid
    @NonNull
    T getDefinition();

    @NotBlank
    @JsonRawValue
    @JsonDeserialize(using = JsonAsStringDeserializer.class)
    String getParams();

}
