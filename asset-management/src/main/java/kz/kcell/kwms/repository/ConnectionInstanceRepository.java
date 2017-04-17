package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.ConnectionInstance;
import kz.kcell.kwms.model.EquipmentInstance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ConnectionInstanceRepository extends PagingAndSortingRepository<ConnectionInstance, Long> {

    @Query("select distinct c from ConnectionInstance c join c.equipments e where e.id in ?1 order by c.id")
    List<ConnectionInstance> findByEquipments(@Param("equipments") Collection<Long> equipments);
}
