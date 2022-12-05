package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.PowerSource;
import kz.kcell.kwms.model.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface PowerSourceRepository extends PagingAndSortingRepository<PowerSource, Long> {

    Page<PowerSource> findBySite(@Param("site") Site site, Pageable p);
}
