package org.kgrid.adapter.v8;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.kgrid.adapter.api.AdapterClientErrorException;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.AdapterServerErrorException;
import org.kgrid.adapter.api.Executor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class V8Executor implements Executor {

    public Value function;

    public V8Executor(Value function) {
        this.function = function;
    }

    @Override
    public synchronized Object execute(Object input, String contentType) {
        try {
            Value result = function.execute(createSharable(Value.asValue(input)));
            return createSharable(result);
        } catch (PolyglotException e) {
            if (e.isGuestException()) {
                throw new AdapterClientErrorException(e.getMessage(), e);
            } else {
                throw new AdapterServerErrorException(e.getMessage(), e);
            }
        } catch (Exception e) {
            throw new AdapterException(e.getMessage(), e);
        }
    }

    // Method to de-contextualize a value which may be passed between objects
    // Remove when https://github.com/oracle/graal/issues/631#issuecomment-716611797 is fixed
    private static Object createSharable(Value v) {
        if (v.isBoolean()) {
            return v.asBoolean();
        } else if (v.isNumber()) {
            return v.as(Number.class);
        } else if (v.isString()) {
            return v.asString();
        } else if (v.isHostObject()) {
            return v.asHostObject();
        } else if (v.isProxyObject()) {
            return v.asProxyObject();
        } else if (v.hasArrayElements()) {
            ArrayList<Object> list = new ArrayList<>();
            for (long i = 0; i < v.getArraySize(); i++) {
                list.add(createSharable(v.getArrayElement(i)));
            }
            return list;
        } else if (v.hasMembers()) {
            Map<String, Object> map = new LinkedHashMap<>();
            v.getMemberKeys().forEach(key -> map.put(key, createSharable(v.getMember(key))));
            return map;
        } else {
            throw new UnsupportedOperationException("Unsupported value");
        }
    }
}
