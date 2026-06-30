package com.aegis.backend.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonLayout extends LayoutBase<ILoggingEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String doLayout(final ILoggingEvent event) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("timestamp", event.getTimeStamp());
        map.put("level", event.getLevel().toString());
        map.put("thread", event.getThreadName());
        map.put("logger", event.getLoggerName());
        map.put("message", event.getFormattedMessage());

        final String correlationId = event.getMDCPropertyMap().get("correlationId");
        if (correlationId != null) {
            map.put("correlationId", correlationId);
        }

        final String requestId = event.getMDCPropertyMap().get("requestId");
        if (requestId != null) {
            map.put("requestId", requestId);
        }

        if (event.getThrowableProxy() != null) {
            map.put("exception", formatException(event.getThrowableProxy()));
        }

        try {
            return objectMapper.writeValueAsString(map) + "\n";
        } catch (final Exception exception) {
            return "{\"error\":\"Serialization failed: " + exception.getMessage() + "\"}\n";
        }
    }

    private String formatException(final IThrowableProxy throwableProxy) {
        final StringBuilder builder = new StringBuilder();
        builder.append(throwableProxy.getClassName())
                .append(": ")
                .append(throwableProxy.getMessage())
                .append("\n");
        if (throwableProxy.getStackTraceElementProxyArray() != null) {
            for (final StackTraceElementProxy step : throwableProxy.getStackTraceElementProxyArray()) {
                builder.append("\tat ").append(step.getSTEAsString()).append("\n");
            }
        }
        return builder.toString();
    }
}
