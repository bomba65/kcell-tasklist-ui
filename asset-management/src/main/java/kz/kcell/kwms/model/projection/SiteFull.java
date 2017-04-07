package kz.kcell.kwms.model.projection;

import com.fasterxml.jackson.annotation.JsonRawValue;
import kz.kcell.kwms.model.Site;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;
import java.util.Set;

@Projection(name = "full", types = Site.class)
public interface SiteFull {
    String getId();
    String getName();
    @JsonRawValue
    String getParams();

    Set<FacilityInstanceFull> getFacilities();
    Set<Site> getFarEndCandidates();
    List<InstallationInstanceFull> getInstallations();
    List<PowerSourceFull> getPowerSources();
}
