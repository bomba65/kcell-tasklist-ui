package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.ConnectionInstance;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ConnectionInstanceRepository extends PagingAndSortingRepository<ConnectionInstance, Long> {

}
