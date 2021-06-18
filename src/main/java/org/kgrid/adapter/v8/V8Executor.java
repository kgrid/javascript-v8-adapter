package org.kgrid.adapter.v8;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.kgrid.adapter.api.AdapterClientErrorException;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.AdapterServerErrorException;
import org.kgrid.adapter.api.Executor;

public class V8Executor implements Executor {

    public Value function;

    public V8Executor(Value function) {
        this.function = function;
    }

    @Override
    public synchronized Object execute(Object input, String contentType) {
        try {
            input = createSharable(Value.asValue(input));
            Value result = function.execute(input);
            return result.as(Object.class);
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
            return new ProxyArray() {
                @Override
                public void set(long index, Value value) {
                    v.setArrayElement(index, createSharable(value));
                }

                @Override
                public long getSize() {
                    return v.getArraySize();
                }

                @Override
                public Object get(long index) {
                    return createSharable(v.getArrayElement(index));
                }

                @Override
                public boolean remove(long index) {
                    return v.removeArrayElement(index);
                }
            };
        } else if (v.hasMembers()) {
            return new ProxyObject() {

                @Override
                public void putMember(String key, Value value) {
                    v.putMember(key, createSharable(value));
                }

                @Override
                public boolean hasMember(String key) {
                    return v.hasMember(key);
                }

                @Override
                public Object getMemberKeys() {
                    return v.getMemberKeys().toArray(new String[0]);
                }

                @Override
                public Object getMember(String key) {
                    return createSharable(v.getMember(key));
                }

                @Override
                public boolean removeMember(String key) {
                    return v.removeMember(key);
                }
            };
        } else {
            throw new UnsupportedOperationException("Unsupported value");
        }
    }

}
