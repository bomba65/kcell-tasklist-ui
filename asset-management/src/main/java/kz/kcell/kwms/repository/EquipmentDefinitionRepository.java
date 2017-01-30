package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.EquipmentDefinition;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentDefinitionRepository extends PagingAndSortingRepository<EquipmentDefinition, Long> {

    List<EquipmentDefinition> findByName(@Param("name") String name);
}
