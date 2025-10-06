package com.btlab.fdcalculator.scheduler;

import com.btlab.fdcalculator.service.RateCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RateCacheScheduler {

    private final RateCacheService rateCacheService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void refreshDaily() {
        List<String> productCodes = List.of("FD_STD","FD_1YR","FD_5YR");
        productCodes.forEach(rateCacheService::refreshRate);
    }
}
