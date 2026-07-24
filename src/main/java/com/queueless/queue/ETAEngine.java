package com.queueless.queue;

import com.queueless.domain.entity.ServiceEntity;
import com.queueless.domain.repository.CounterServiceMappingRepository;
import com.queueless.domain.repository.ServiceRepository;
import com.queueless.domain.repository.TokenRepository;
import com.queueless.dto.response.ETAResponse;
import org.springframework.stereotype.Component;

@Component
public class ETAEngine {

    private final TokenRepository tokenRepository;
    private final ServiceRepository serviceRepository;
    private final CounterServiceMappingRepository counterServiceMappingRepository;

    public ETAEngine(TokenRepository tokenRepository,
                      ServiceRepository serviceRepository,
                      CounterServiceMappingRepository counterServiceMappingRepository) {
        this.tokenRepository = tokenRepository;
        this.serviceRepository = serviceRepository;
        this.counterServiceMappingRepository = counterServiceMappingRepository;
    }

    public ETAResponse calculateETA(Long serviceId, int position) {
        ServiceEntity service = serviceRepository.findById(serviceId).orElse(null);
        int avgDuration = (service != null && service.getAvgDurationMin() != null) ? service.getAvgDurationMin() : 10;

        int mappedCountersCount = counterServiceMappingRepository.findByServiceId(serviceId).size();
        int activeCounters = Math.max(1, mappedCountersCount);

        int estimatedWaitMinutes = (int) Math.ceil(((double) position * avgDuration) / activeCounters);

        return ETAResponse.builder()
                .serviceId(serviceId)
                .position(position)
                .estimatedWaitMinutes(estimatedWaitMinutes)
                .activeCounters(activeCounters)
                .avgServiceDurationMinutes(avgDuration)
                .build();
    }
}
