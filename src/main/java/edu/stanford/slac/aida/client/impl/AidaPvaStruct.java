package edu.stanford.slac.aida.client.impl;

import java.util.Arrays;
import java.util.HashMap;

public class AidaPvaStruct extends HashMap<String, Object> {
    @Override
    public Object put(String key, Object value) {
        if (value instanceof Object[]) {
            value = Arrays.asList((Object[]) value);
        }
        return super.put(key, value);
    }
}
