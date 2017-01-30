package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Site;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.validation.Valid;
import java.util.List;

public interface SiteRepository extends PagingAndSortingRepository<Site, String> {

    List<Site> findByName(@Param("name") String name);

}
