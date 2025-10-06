package com.btlab.fdcalculator.config;

import com.btlab.fdcalculator.client.PricingApiClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableFeignClients(clients = PricingApiClient.class) // Tells Spring to scan for this Feign client
@Profile("!mock") // The crucial part: Only activate this configuration when the profile is NOT 'mock'
public class FeignClientConfig {
}