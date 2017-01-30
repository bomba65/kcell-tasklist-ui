package kz.kcell.kwms.repository.custom;

import kz.kcell.kwms.model.Facility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.web.PageableDefault;

import java.util.List;

public interface FacilityRepositoryCustom {
    Page<Facility> findByLocationNear(
            @Param("long") double longitude,
            @Param("lat") double latitude,
            Pageable pageable
    );

}
