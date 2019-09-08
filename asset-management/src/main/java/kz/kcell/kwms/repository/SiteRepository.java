package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Transactional
public interface SiteRepository extends PagingAndSortingRepository<Site, Long> {

    List<Site> findByName(@Param("name") String name);
    Page<Site> findByNameIgnoreCaseContaining(@Param("name") String name, Pageable p);
}
