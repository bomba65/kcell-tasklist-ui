package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.InstallationDefinition;
import kz.kcell.kwms.model.InstallationInstance;
import kz.kcell.kwms.model.Site;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstallationInstanceRepository extends PagingAndSortingRepository<InstallationInstance, Long> {
    Page<InstallationInstance> findBySite(@Param("site") Site site, Pageable p);

    List<InstallationInstance> findBySiteAndDefinition(@Param("site") Site site, @Param("definition")InstallationDefinition definition);
}
