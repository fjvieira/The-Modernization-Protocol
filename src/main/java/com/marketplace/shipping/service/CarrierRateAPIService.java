package com.marketplace.shipping.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.marketplace.shipping.dto.CalculateShippingRequest.Parcel;
import com.marketplace.shipping.dto.CarrierRateResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j @Service
public class CarrierRateAPIService {

    private final RestTemplate restTemplate;
    private final String carrierUrl;

    public CarrierRateAPIService(RestTemplate restTemplate,
            @Value("${external.carrier.url}") String carrierUrl) {
        this.restTemplate = restTemplate;
        this.carrierUrl = carrierUrl;
    }

    public Optional<CarrierRateResponse> fetchCarrierRate(String countryCode, String zipCode,
            Parcel parcel) {
        log.info("Requesting carrier rate for country={}, zip={}", countryCode, zipCode);
        Map<String, Object> requestPayload = Map.of("countryCode", countryCode, "zipCode", zipCode, "parcel",
                parcel != null ? parcel : Map.of());
        log.info("Carrier request payload={}", requestPayload);

        try {
            CarrierRateResponse response = restTemplate.postForObject(carrierUrl, requestPayload,
                    CarrierRateResponse.class);
            if (response == null || response.getBaseRate() == null) {
                log.warn("Carrier has no rate for country={}, zip={}", countryCode, zipCode);
                return Optional.empty();
            }
            log.info("Carrier response payload={}", response);
            log.info("Carrier rate received baseRate={}, carrier={}, surcharge={}",
                    response.getBaseRate(), response.getCarrierCode(), response.getSurcharge());
            return Optional.of(response);
        } catch (HttpClientErrorException.NotFound e) {
            log.info("Carrier does not support country={}, zip={}", countryCode, zipCode);
            return Optional.empty();
        } catch (RestClientException e) {
            log.error("Carrier rate request failed for country={}, zip={}", countryCode, zipCode, e);
            throw new IllegalStateException("Failed to communicate with external carrier service",
                    e);
        }
    }
}