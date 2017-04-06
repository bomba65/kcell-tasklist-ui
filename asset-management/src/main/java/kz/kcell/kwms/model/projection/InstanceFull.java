package kz.kcell.kwms.model.projection;

import com.fasterxml.jackson.annotation.JsonRawValue;
import org.springframework.beans.factory.annotation.Value;

public interface InstanceFull {
    Long getId();
    @JsonRawValue
    String getParams();
    @Value("#{target.definition.id}")
    String getDefinitionId();
}
