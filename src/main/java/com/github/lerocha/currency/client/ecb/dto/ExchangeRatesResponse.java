package com.github.lerocha.currency.client.ecb.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Created by lerocha on 2/20/17.
 */
@JacksonXmlRootElement(namespace = "gesmes")
public class ExchangeRatesResponse {
    @JacksonXmlProperty(namespace = "gesmes")
    private String subject;
    @JacksonXmlProperty(namespace = "gesmes", localName = "Sender")
    private Sender sender;
    @JacksonXmlElementWrapper(localName = "Cube")
    private List<DailyExchangeRate> dailyExchangeRates;

    public List<DailyExchangeRate> getDailyExchangeRates() {
        return dailyExchangeRates;
    }

    public void setDailyExchangeRates(List<DailyExchangeRate> cube) {
        this.dailyExchangeRates = cube;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("subject=").append(subject)
                .append("; sender=").append(sender)
                .toString();
    }
}