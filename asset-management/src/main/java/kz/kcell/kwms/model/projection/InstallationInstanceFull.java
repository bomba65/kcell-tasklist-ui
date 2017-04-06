package kz.kcell.kwms.model.projection;

import kz.kcell.kwms.model.FacilityInstance;
import kz.kcell.kwms.model.InstallationDefinition;
import kz.kcell.kwms.model.InstallationInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(types = InstallationInstance.class)
public interface InstallationInstanceFull extends InstanceFull {
    @Value("#{target.facility.id}")
    Long getFacilityId();
    EquipmentInstanceFull getEquipment();
}
