package com.github.lerocha.currency.repository;

import com.github.lerocha.currency.domain.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by lerocha on 2/25/17.
 */
@RepositoryRestResource
public interface CurrencyRepository extends JpaRepository<Currency, String> {
    @Modifying
    @Query(nativeQuery = true, value = "UPDATE currency c CROSS JOIN " +
            "(SELECT currency_code, min(exchange_date) AS start_date, max(exchange_date) AS end_date FROM exchange_rate GROUP BY currency_code) cdate\n" +
            "SET c.start_date = cdate.start_date, c.end_date = cdate.end_date\n" +
            "WHERE c.code = cdate.currency_code")
    int updateCurrencyStartAndEndDates();
}