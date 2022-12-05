package kz.kcell.kwms.repository.custom;

import kz.kcell.kwms.model.FacilityInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface FacilityInstanceRepositoryCustom {
    Page<FacilityInstance> findByLocationNear(
            @Param("long") double longitude,
            @Param("lat") double latitude,
            Pageable pageable
    );

}
