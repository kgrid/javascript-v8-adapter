package org.kgrid.adapter.v8;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
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
}
