package me.flame.storysmp.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Registry {
    private final Map<String, Object> elements = new HashMap<>();

    public <T> void put(String key, T value) {
        elements.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) elements.get(key);
    }

    public <T> T get(String key, Class<T> clazz) {
        return clazz.cast(elements.get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(String key, Function<String, ? super T> function) {
        return (T) elements.computeIfAbsent(key, function);
    }
}
