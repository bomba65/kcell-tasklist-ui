package kz.kcell.kwms.model.projection;

import kz.kcell.kwms.model.ConnectionInstance;
import kz.kcell.kwms.model.EquipmentInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;
import java.util.SortedSet;

@Projection(name = "id", types = ConnectionInstance.class)
public interface ConnectionInstanceId {
    @Value("/connectionInstances/#{target.id}")
    String getId();
}
