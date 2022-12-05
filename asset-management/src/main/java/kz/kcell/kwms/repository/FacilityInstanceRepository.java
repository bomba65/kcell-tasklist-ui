package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.FacilityInstance;
import kz.kcell.kwms.repository.custom.FacilityInstanceRepositoryCustom;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FacilityInstanceRepository extends PagingAndSortingRepository<FacilityInstance, Long>, FacilityInstanceRepositoryCustom {

}
