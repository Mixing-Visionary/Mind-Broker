package ru.visionary.mixing.mind_broker.client.error;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import java.io.IOException;

@Slf4j
public class MegamindErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String s, Response response) {
        try {
            log.error("Error on calling Megamind: {}", new String(response.body().asInputStream().readAllBytes()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (response.status() >= 500) {
            return new ServiceException(ErrorCode.MEGAMIND_ERROR);
        } else if (response.status() >= 400) {
            return new ServiceException(ErrorCode.INTERNAL_ERROR);
        }
        return defaultDecoder.decode(s, response);
    }
}
