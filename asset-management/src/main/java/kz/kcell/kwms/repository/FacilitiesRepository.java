package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Facility;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.util.Collection;
import java.util.List;

public interface FacilitiesRepository extends PagingAndSortingRepository<Facility, Long> {

    List<Facility> findByName(@Param("name") String name);

    @Query("select f from Facility f where f.name like %?1%")
    List<Facility> findByNameLike(@Param("name") String name);

    List<Facility> findBySiteName(@Param("name") String name);

}
