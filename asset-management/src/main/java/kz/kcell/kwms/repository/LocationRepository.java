package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Location;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LocationRepository extends PagingAndSortingRepository<Location, Long> {

    @Query("select l from Location l where l.sitename = ?1")
    List<Location> findBySiteName(@Param("sitename") String sitename);

    @Query("select l from Location l where l.name = ?1")
    List<Location> getByName(@Param("name") String name);
}
