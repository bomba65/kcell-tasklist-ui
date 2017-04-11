package kz.kcell.kwms.model.projection;

import com.fasterxml.jackson.annotation.JsonRawValue;
import kz.kcell.kwms.model.PowerSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "full", types = PowerSource.class)
public interface PowerSourceFull {
    Long getId();

    @Value("/sites/#{target.site.id}")
    String getSite();

    @JsonRawValue
    String getParams();
}
