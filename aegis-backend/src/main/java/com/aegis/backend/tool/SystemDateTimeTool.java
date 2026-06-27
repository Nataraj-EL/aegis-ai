package com.aegis.backend.tool;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class SystemDateTimeTool implements Tool {

    public SystemDateTimeTool() {
        // Default constructor
    }

    @Override
    public String getId() {
        return "system_datetime";
    }

    @Override
    public String getName() {
        return "System Date & Time Tool";
    }

    @Override
    public String getDescription() {
        return "Returns the current system date and time.";
    }

    @Override
    public Map<String, Object> getParametersSchema() {
        return Collections.emptyMap();
    }

    @Override
    public Object execute(final Map<String, Object> arguments) {
        return LocalDateTime.now().toString();
    }
}
