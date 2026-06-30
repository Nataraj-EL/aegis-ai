package com.aegis.backend.observability;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_CORRELATION_ID = "correlationId";

    public CorrelationIdFilter() {
        // Default constructor
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final HttpServletResponse httpResponse = (HttpServletResponse) response;

            String correlationId = httpRequest.getHeader(REQUEST_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            }
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            MDC.put(MDC_REQUEST_ID, correlationId);
            MDC.put(MDC_CORRELATION_ID, correlationId);
            httpResponse.setHeader(REQUEST_ID_HEADER, correlationId);
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            final long startTime = System.currentTimeMillis();
            try {
                chain.doFilter(request, response);
            } finally {
                final long duration = System.currentTimeMillis() - startTime;
                log.info(
                        "HTTP Request processed: method={}, uri={}, status={}, duration={}ms",
                        httpRequest.getMethod(),
                        httpRequest.getRequestURI(),
                        httpResponse.getStatus(),
                        duration);
                MDC.clear();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
