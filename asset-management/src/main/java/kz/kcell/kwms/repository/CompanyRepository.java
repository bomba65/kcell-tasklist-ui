package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Company;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CompanyRepository extends PagingAndSortingRepository<Company, Long> {
}
