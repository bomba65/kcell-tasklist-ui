package kz.kcell.kwms.model.projection;

import com.fasterxml.jackson.annotation.JsonRawValue;
import kz.kcell.kwms.model.ConnectionInstance;
import kz.kcell.kwms.model.EquipmentInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;
import java.util.SortedSet;

@Projection(name = "full", types = EquipmentInstance.class)
public interface EquipmentInstanceFull {
    Long getId();

    @Value("/equipmentInstances/")
    String getSegment();

    String getSn();

    @JsonRawValue
    String getParams();

    @Value("#{target.definition.id}")
    String getDefinition();

    List<ConnectionInstanceId> getConnections();
}
