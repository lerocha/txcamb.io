package com.github.lerocha.currency.service;

import com.github.lerocha.currency.client.ecb.EcbClient;
import com.github.lerocha.currency.client.ecb.dto.CurrencyExchangeRate;
import com.github.lerocha.currency.client.ecb.dto.DailyExchangeRate;
import com.github.lerocha.currency.client.ecb.dto.ExchangeRatesResponse;
import com.github.lerocha.currency.domain.ExchangeRate;
import com.github.lerocha.currency.dto.HistoricalExchangeRate;
import com.github.lerocha.currency.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lerocha on 2/1/17.
 */
@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);

    private final ExchangeRateRepository exchangeRateRepository;
    private final EcbClient ecbClient;

    @Autowired
    public ExchangeRateServiceImpl(ExchangeRateRepository exchangeRateRepository,
                                   EcbClient ecbClient) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.ecbClient = ecbClient;
    }

    private static final String DEFAULT_BASE = "EUR";

    @Override
    public HistoricalExchangeRate getLatestExchangeRate(String base) {
        LocalDate date = exchangeRateRepository.findMaxExchangeDate();
        if (date == null) {
            return null;
        }

        return getHistoricalExchangeRate(date, base);
    }

    @Override
    public HistoricalExchangeRate getHistoricalExchangeRate(LocalDate date, String base) {
        Assert.notNull(date);
        List<ExchangeRate> rates = exchangeRateRepository.findByExchangeDateOrderByCurrencyCode(date);
        logger.info("getHistoricalExchangeRate; date={}; base={}", date, base);
        return getHistoricalExchangeRate(date, base, rates);
    }

    private HistoricalExchangeRate getHistoricalExchangeRate(LocalDate date, String base, List<ExchangeRate> rates) {
        Assert.notNull(date);
        Assert.notNull(rates);
        HistoricalExchangeRate historicalExchangeRate = new HistoricalExchangeRate(date, base != null ? base : DEFAULT_BASE);
        BigDecimal baseRate = null;
        rates.add(new ExchangeRate(date, DEFAULT_BASE, BigDecimal.ONE.setScale(6, BigDecimal.ROUND_HALF_UP)));
        for (ExchangeRate rate : rates.stream().sorted(Comparator.comparing(o -> o.getCurrencyCode())).collect(Collectors.toList())) {
            historicalExchangeRate.getRates().put(rate.getCurrencyCode(), rate.getExchangeRate());
            if (rate.getCurrencyCode().equalsIgnoreCase(base)) {
                baseRate = rate.getExchangeRate();
            }
        }

        if (baseRate != null) {
            for (Map.Entry<String, BigDecimal> entry : historicalExchangeRate.getRates().entrySet()) {
                entry.setValue(entry.getValue().divide(baseRate, baseRate.scale(), BigDecimal.ROUND_CEILING));
            }
        }
        return historicalExchangeRate;
    }

    @Override
    public List<HistoricalExchangeRate> getHistoricalExchangeRates(LocalDate startDate, LocalDate endDate, String base) {
        List<HistoricalExchangeRate> historicalExchangeRates = new ArrayList<>();
        if (startDate == null) {
            startDate = exchangeRateRepository.findMinExchangeDate();
        }
        if (endDate == null) {
            endDate = exchangeRateRepository.findMaxExchangeDate();
        }
        List<ExchangeRate> allRates = exchangeRateRepository.findByExchangeDateBetweenOrderByExchangeDate(startDate, endDate);
        List<ExchangeRate> rates = new ArrayList<>();
        LocalDate date = null;
        for (ExchangeRate rate : allRates) {
            if (date == null) {
                date = rate.getExchangeDate();
            } else if (!date.equals(rate.getExchangeDate())) {
                historicalExchangeRates.add(getHistoricalExchangeRate(date, base, rates));
                rates.clear();
                date = rate.getExchangeDate();
            }
            rates.add(rate);
        }
        if (rates.size() > 0) {
            historicalExchangeRates.add(getHistoricalExchangeRate(date, base, rates));
        }

        logger.info("getHistoricalExchangeRates; startDate={}; endDate={}; base={}; total={}", startDate, endDate, base, historicalExchangeRates.size());
        return historicalExchangeRates;
    }

    @Override
    public List<ExchangeRate> refreshExchangeRates() {
        LocalDate lastRefresh = exchangeRateRepository.findMaxExchangeDate();
        logger.info("refreshExchangeRates; status=starting; lastRefresh={}", lastRefresh);
        ResponseEntity<ExchangeRatesResponse> response;
        if (lastRefresh == null || lastRefresh.isBefore(LocalDate.now().minusDays(90))) {
            // Full refresh if more than 90 days since last refresh.
            response = ecbClient.getAllExchangeRates();
        } else if (lastRefresh.isBefore(LocalDate.now().minusDays(1))) {
            // Partial refresh if less than 90 days since last refresh.
            response = ecbClient.getLast90DaysExchangeRates();
        } else {
            // Daily refresh.
            response = ecbClient.getCurrentExchangeRates();
        }
        if (!response.getStatusCode().is2xxSuccessful() ||
                response.getBody() == null ||
                response.getBody().getDailyExchangeRates() == null) {
            logger.error("refreshExchangeRates; status={}; body={}", response.getStatusCode(), response.getBody());
            return null;
        }

        // Filter and sort results.
        List<DailyExchangeRate> dailyExchangeRates = response.getBody().getDailyExchangeRates().stream()
                .filter(o -> lastRefresh == null || o.getDate().isAfter(lastRefresh))
                .sorted(Comparator.comparing(DailyExchangeRate::getDate))
                .collect(Collectors.toList());

        // Convert into entity objects.
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        for (DailyExchangeRate dailyExchangeRate : dailyExchangeRates) {
            exchangeRates.addAll(dailyExchangeRate.getCurrencyExchangeRates().stream()
                    .sorted(Comparator.comparing(CurrencyExchangeRate::getCurrency))
                    .map(o -> new ExchangeRate(dailyExchangeRate.getDate(), o.getCurrency(), o.getRate()))
                    .collect(Collectors.toList()));
        }

        // Bulk save.
        exchangeRates = (List<ExchangeRate>) exchangeRateRepository.save(exchangeRates);
        logger.info("refreshExchangeRates; status=ok; startDate={}; endDate={}; total={}",
                exchangeRates.size() > 0 ? exchangeRates.get(0).getExchangeDate() : null,
                exchangeRates.size() > 0 ? exchangeRates.get(exchangeRates.size() - 1).getExchangeDate() : null,
                exchangeRates.size());
        return exchangeRates;
    }
}
