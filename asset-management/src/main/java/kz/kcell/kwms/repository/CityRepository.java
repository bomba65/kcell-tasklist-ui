package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.City;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CityRepository extends PagingAndSortingRepository<City, Long> {
}
