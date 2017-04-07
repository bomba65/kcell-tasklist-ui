package kz.kcell.kwms.model.projection;

import kz.kcell.kwms.model.EquipmentInstance;
import org.springframework.data.rest.core.config.Projection;

@Projection(types = EquipmentInstance.class)
public interface EquipmentInstanceFull extends InstanceFull {
    String getSn();
}
