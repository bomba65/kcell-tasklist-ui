package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.*;
import kz.kcell.kwms.repository.custom.FacilityRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

    public interface FacilityInstanceRepository extends PagingAndSortingRepository<FacilityInstance, Long>, FacilityRepositoryCustom {

    Page<FacilityInstance> findBySite(@Param("site") Site site, Pageable p);

    List<FacilityInstance> findBySiteAndDefinition(@Param("site") Site site, @Param("definition") FacilityDefinition definition);
}
