package kz.kcell.kwms.repository;

import jdk.nashorn.internal.parser.JSONParser;
import kz.kcell.kwms.model.Plan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//public interface InstallationInstanceRepository extends PagingAndSortingRepository<InstallationInstance, Long> {
//    Page<InstallationInstance> findBySite(@Param("site") Site site, Pageable p);
//
//    List<InstallationInstance> findBySiteAndDefinition(@Param("site") Site site, @Param("definition")InstallationDefinition definition);
//}

@Transactional
public interface PlanRepository extends PagingAndSortingRepository<Plan, Long> {
    @Override
    Page<Plan> findAll(Pageable pageable);

    @Query("select distinct l from Plan l where l.position_number = ?1 and l.is_current = true")
    List<Plan> findBySite(@Param("position_number") Integer position_number);

    @Query("select l from Plan l where l.is_current = true")
    List<Plan> findCurrentPlanSites();

    @Query("select l from Plan l where l.is_current = true and l.status = 'candidate_sharing'")
    List<Plan> findCurrentToStartPlanSites();

    @Modifying
    @Query("update Plan l set l.is_current = false where l.position_number = ?1 and l.is_current = true")
    void changePrevCurrentStatus(@Param("position_number") Integer position_number);

    @Modifying
    @Query("update Plan l set l.status = ?2 where l.position_number = ?1 and l.is_current = true")
    void changePlanStatus(@Param("position_number") Integer position_number, @Param("status") String status);

}
