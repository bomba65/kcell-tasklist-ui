package kz.kcell.kwms.model.projection;

import kz.kcell.kwms.model.ConnectionInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "full", types = ConnectionInstance.class)
public interface ConnectionInstanceFull {
    String getId();

    @Value("#{target.definition.id}")
    String getDefinition();

    @Value("#{target.equipments.![id]}")
    List<Long> getEquipments();
}
