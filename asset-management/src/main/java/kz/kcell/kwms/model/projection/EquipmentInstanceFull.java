package kz.kcell.kwms.model.projection;

import com.fasterxml.jackson.annotation.JsonRawValue;
import kz.kcell.kwms.model.EquipmentInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "full", types = EquipmentInstance.class)
public interface EquipmentInstanceFull {
    Long getId();

    String getSn();

    @JsonRawValue
    String getParams();

    @Value("/equipmentDefinitions/#{target.definition.id}")
    String getDefinition();
}
