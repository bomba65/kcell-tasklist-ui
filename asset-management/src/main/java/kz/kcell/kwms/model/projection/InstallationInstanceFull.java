package kz.kcell.kwms.model.projection;

import com.fasterxml.jackson.annotation.JsonRawValue;
import kz.kcell.kwms.model.InstallationInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "full", types = InstallationInstance.class)
public interface InstallationInstanceFull {
    Long getId();

    @JsonRawValue
    String getParams();

    @Value("#{target.definition.id}")
    String getDefinition();

    @Value("#{target?.facility?.id}")
    String getFacility();

    EquipmentInstanceFull getEquipment();
}
