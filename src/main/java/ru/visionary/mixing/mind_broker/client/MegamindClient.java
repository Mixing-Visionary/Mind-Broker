package ru.visionary.mixing.mind_broker.client;

import feign.Request.Options;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.visionary.mixing.mind_broker.client.dto.GetStylesResponse;
import ru.visionary.mixing.mind_broker.client.dto.ImageProcessingRequest;
import ru.visionary.mixing.mind_broker.client.dto.ImageProcessingResponse;

@FeignClient(value = "megamind")
public interface MegamindClient {
    @PostMapping("/api/v1/process")
    ImageProcessingResponse process(Options options, @RequestBody ImageProcessingRequest request);

    @GetMapping("/api/v1/styles")
    GetStylesResponse getStyles();
}
