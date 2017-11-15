package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Plan;
import kz.kcell.kwms.model.Site;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Transactional
public interface PlanRepository extends PagingAndSortingRepository<Plan, Long> {

    @Query("select distinct l from Plan l join l.site s where s.id = ?1 and l.is_current = true")
    List<Plan> findBySite(@Param("siteId") Long siteId);

    @Query("select l.site from Plan l where l.is_current = true and l.status = 'candidate_sharing'")
    List<Site> findCurrentPlanSites();

    @Modifying
    @Query("update Plan l set l.is_current = false where l.site.id = ?1 and l.is_current = true")
    void changePrevCurrentStatus(@Param("siteId") Long siteId);

    @Modifying
    @Query("update Plan l set l.status = ?2 where l.site.id = ?1 and l.is_current = true")
    void changePlanStatus(@Param("siteId") Long siteId, @Param("status") String status);
}
