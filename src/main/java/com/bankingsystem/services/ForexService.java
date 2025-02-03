package com.bankingSystem.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ForexService {
    private static final String API_URL = "https://api.exchangeratesapi.io/latest?base=";

    public double convert(String fromCurrency, String toCurrency, double amount) {
        RestTemplate restTemplate = new RestTemplate();
        String url = API_URL + fromCurrency;
        Map response = restTemplate.getForObject(url, Map.class);

        Map<String, Double> rates = (Map<String, Double>) response.get("rates");
        double rate = rates.get(toCurrency);

        return amount * rate;
    }
}

