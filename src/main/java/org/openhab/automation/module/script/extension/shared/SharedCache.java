package org.openhab.automation.module.script.extension.shared;

import org.graalvm.polyglot.Value;

import java.util.HashMap;
import java.util.Map;

public class SharedCache {

    private Map<String, Value> cache = new HashMap<>();

    public void put(String k, Value v) {
        cache.put(k, v);
    }

    public Value get(String k) {
        return cache.get(k);
    }

    public Value executeValue(String k, Object... params) {
        return get(k).execute(params);
    }
}
