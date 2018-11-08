package kz.kcell.kwms.repository;

import kz.kcell.kwms.model.Plan;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CurrencyRateRepository extends PagingAndSortingRepository<Plan, Long> {

    @Query(value = "select value from currency_exchange_rates where currency_exchange_direction = 'USD_TO_KK'", nativeQuery = true)
    List<Double> getCurrentExchangeRate();
}
