package kz.kcell.kwms.model.projection;

import com.vividsolutions.jts.geom.Point;
import kz.kcell.kwms.model.FacilityDefinition;
import kz.kcell.kwms.model.FacilityInstance;
import org.springframework.data.rest.core.config.Projection;

@Projection(types = FacilityInstance.class)
public interface FacilityInstanceFull extends InstanceFull {
    Point getLocation();
}
