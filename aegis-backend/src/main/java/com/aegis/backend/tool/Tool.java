package com.aegis.backend.tool;

import java.util.Map;

public interface Tool {
    String getId();

    String getName();

    String getDescription();

    Map<String, Object> getParametersSchema();

    Object execute(Map<String, Object> arguments);
}
