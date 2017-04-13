package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.ConnectionDefinition;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ConnectionDefinitionRepository extends PagingAndSortingRepository<ConnectionDefinition, String> {
}
