package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Facility;
import kz.kcell.kwms.repository.custom.FacilityRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

public interface FacilityRepository extends PagingAndSortingRepository<Facility, Long>, FacilityRepositoryCustom {

    List<Facility> findByName(@Param("name") String name);

    @Query("select f from Facility f where f.name like %?1%")
    List<Facility> findByNameLike(@Param("name") String name);


}
