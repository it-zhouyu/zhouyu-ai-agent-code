package com.zhouyu.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：IT周瑜
 * 公众号：IT周瑜
 * 微信号：it_zhouyu
 */
public abstract class BaseTool implements Tool {
    protected final String name;
    protected final String description;

    protected BaseTool(String name, String description) {
        this.name = name;
        this.description = description;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }


    protected Map<String, Object> stringParam(String description) {
        Map<String, Object> param = new HashMap<>();
        param.put("type", "string");
        param.put("description", description);
        return param;
    }

    protected Map<String, Object> boolParam(String description) {
        Map<String, Object> param = new HashMap<>();
        param.put("type", "boolean");
        param.put("description", description);
        return param;
    }

    protected Map<String, Object> intParam(String description) {
        Map<String, Object> param = new HashMap<>();
        param.put("type", "integer");
        param.put("description", description);
        return param;
    }

    protected Map<String, Object> enumParam(String description, List<String> values) {
        Map<String, Object> param = new HashMap<>();
        param.put("type", "string");
        param.put("description", description);
        param.put("enum", values);
        return param;
    }

    protected String getString(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        return value != null ? value.toString() : null;
    }

    protected String getString(Map<String, Object> parameters, String key, String defaultValue) {
        String value = getString(parameters, key);
        return value != null ? value : defaultValue;
    }

    protected Boolean getBoolean(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    protected boolean getBoolean(Map<String, Object> parameters, String key, boolean defaultValue) {
        Boolean value = getBoolean(parameters, key);
        return value != null ? value : defaultValue;
    }

    protected Integer getInteger(Map<String, Object> parameters, String key) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    protected int getInteger(Map<String, Object> parameters, String key, int defaultValue) {
        Integer value = getInteger(parameters, key);
        return value != null ? value : defaultValue;
    }

    protected Map<String, Object> buildSchema(Map<String, Map<String, Object>> properties, List<String> required) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        if (required != null && !required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }
}