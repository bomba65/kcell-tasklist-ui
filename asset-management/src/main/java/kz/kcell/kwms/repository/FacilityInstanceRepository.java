package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.*;
import kz.kcell.kwms.repository.custom.FacilityInstanceRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FacilityInstanceRepository extends PagingAndSortingRepository<FacilityInstance, Long>, FacilityInstanceRepositoryCustom {

}
