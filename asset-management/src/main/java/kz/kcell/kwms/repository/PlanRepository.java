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

    @Query("select distinct l from Plan l where l.site_id = ?1 and l.is_current = true")
    List<Plan> findBySite(@Param("siteId") Integer siteId);

    @Query("select l from Plan l where l.is_current = true")
    List<Plan> findCurrentPlanSites();

    @Query("select l from Plan l where l.is_current = true and l.status = 'candidate_sharing'")
    List<Plan> findCurrentToStartPlanSites();

    @Modifying
    @Query("update Plan l set l.is_current = false where l.site_id = ?1 and l.is_current = true")
    void changePrevCurrentStatus(@Param("siteId") Integer siteId);

    @Modifying
    @Query("update Plan l set l.status = ?2 where l.site_id = ?1 and l.is_current = true")
    void changePlanStatus(@Param("siteId") Integer siteId, @Param("status") String status);
}
