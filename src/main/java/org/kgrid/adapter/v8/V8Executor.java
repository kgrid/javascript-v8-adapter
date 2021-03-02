package org.kgrid.adapter.v8;

import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.kgrid.adapter.api.AdapterClientErrorException;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.AdapterServerErrorException;
import org.kgrid.adapter.api.Executor;

public class V8Executor implements Executor {

    public Value wrapper;
    public Value function;

    public V8Executor(Value wrapper, Value function) {
        this.wrapper = wrapper;
        this.function = function;
    }

    @Override
    public synchronized Object execute(Object input, String contentType) {
        try {
            Value result = wrapper.execute(function, input, contentType);
            return result.as(Object.class);
        }
        catch (PolyglotException e){
           if(e.isGuestException()){
               throw new AdapterClientErrorException("Code execution error: " + e.getMessage(), e);
           }else {
               throw new AdapterServerErrorException("Code execution error: " + e.getMessage(), e);
           }
        }
        catch (Exception e) {
            throw new AdapterException("Code execution error: " + e.getMessage(), e);
        }
    }

    // Overloaded and waiting for a future where we can simply pass javascript variables between contexts
    // Uncomment when https://github.com/oracle/graal/issues/631#issuecomment-716611797 is fixed
//    public Object execute(Object input) {
//        try {
//            Value result = function.execute(input);
//            return result.as(Object.class);
//        } catch (Exception e) {
//            throw new AdapterException("Code execution error", e);
//        }
//    }

}
