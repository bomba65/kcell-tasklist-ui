package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Site;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SiteRepository extends PagingAndSortingRepository<Site, Long> {

    List<Site> findByName(@Param("name") String name);
    List<Site> findByNameIgnoreCaseContaining(@Param("name") String name);

}
