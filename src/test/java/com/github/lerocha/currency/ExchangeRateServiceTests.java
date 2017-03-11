package com.github.lerocha.currency;

import com.github.lerocha.currency.service.CurrencyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by lerocha on 2/20/17.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ExchangeRateServiceTests {
    @Autowired
    private CurrencyService currencyService;

    @Test
    public void refreshExchangeRates() {
        currencyService.refreshExchangeRates();
    }
}
