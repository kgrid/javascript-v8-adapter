package org.kgrid.adapter.v8;

import org.graalvm.polyglot.Value;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;

public class V8Executor implements Executor {

    private Value wrapper;
    private Value function;

    public V8Executor(Value wrapper, Value function) {
        this.wrapper = wrapper;
        this.function = function;
    }

    @Override
    public Object execute(Object input) {
        try {
            Value result = wrapper.execute(function, input);
            return result.as(Object.class);
        } catch (Exception e) {
            throw new AdapterException("Code execution error", e);
        }
    }

}
