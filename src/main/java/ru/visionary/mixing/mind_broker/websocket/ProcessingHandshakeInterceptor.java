package ru.visionary.mixing.mind_broker.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import ru.visionary.mixing.mind_broker.exception.ErrorCode;
import ru.visionary.mixing.mind_broker.exception.ServiceException;

import java.util.Map;
import java.util.UUID;

public class ProcessingHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            try {
                UUID uuid = UUID.fromString(servletRequest.getServletRequest().getParameter("uuid"));
                attributes.put("uuid", uuid);
            } catch (Exception e) {
                throw new ServiceException(ErrorCode.INVALID_REQUEST);
            }
        }
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }
}
