package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Facility;
import kz.kcell.kwms.model.FacilityDefinition;
import kz.kcell.kwms.repository.custom.FacilityRepositoryCustom;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacilityDefinitionRepository extends PagingAndSortingRepository<FacilityDefinition, String> {

}
