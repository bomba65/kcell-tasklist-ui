package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SiteRepository extends PagingAndSortingRepository<Site, Long> {

    List<Site> findByName(@Param("name") String name);
    Page<Site> findByNameIgnoreCaseContaining(@Param("name") String name, Pageable p);

}
