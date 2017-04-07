package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.EquipmentDefinition;
import kz.kcell.kwms.model.EquipmentInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

public interface EquipmentInstanceRepository extends PagingAndSortingRepository<EquipmentInstance, Long> {

    Page<EquipmentInstance> findBySn(@Param("sn") String sn, Pageable p);

    Page<EquipmentInstance> findByDefinition(@Param("definition") EquipmentDefinition definition, Pageable p);
}
