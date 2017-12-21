package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Location;
import kz.kcell.kwms.model.Site;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LocationRepository extends PagingAndSortingRepository<Location, Long> {

    @Query("select distinct l from Location l join l.site s where s.id = ?1")
    List<Location> findBySite(@Param("siteId") Long siteId);

}
