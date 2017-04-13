package kz.kcell.kwms.model.projection;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.vividsolutions.jts.geom.Point;
import kz.kcell.kwms.model.FacilityInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "full", types = FacilityInstance.class)
public interface FacilityInstanceFull {
    Long getId();

    Point getLocation();

    @JsonRawValue
    String getParams();

    @Value("#{target.definition.id}")
    String getDefinition();
}
