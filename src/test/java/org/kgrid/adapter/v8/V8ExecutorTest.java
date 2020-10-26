package org.kgrid.adapter.v8;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.adapter.api.AdapterException;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class V8ExecutorTest {

    V8Executor v8Executor;

    @Test
    public void executeCallsExecuteOnWrapper() {
        Context context =
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.ALL)
                        .allowExperimentalOptions(true)
                        .option("js.experimental-foreign-object-prototype", "true")
                        .allowHostClassLookup(className -> true)
                        .allowNativeAccess(true)
                        .build();
        String wrapperCode = "function wrapper(baseFunction, arg, contentType) { "
                + "return baseFunction(arg);"
                + "}";
        String baseFunctionCode = "function baseFunction(input){return input+1}";
        context.eval("js", wrapperCode);
        context.eval("js", baseFunctionCode);
        Value wrapper = context.getBindings("js").getMember("wrapper");
        Value baseFunction = context.getBindings("js").getMember("baseFunction");
        v8Executor = new V8Executor(wrapper, baseFunction);
        int result = (int) v8Executor.execute(5, "text/plain");
        assertEquals(6, result);
    }

    @Test
    public void executeThrowsAdapterExceptionWhenExecuteFails() {

        v8Executor = new V8Executor(null, null);
        try {
            v8Executor.execute(5, "text/plain");
            fail();
        } catch (AdapterException e) {
            assertEquals("Code execution error", e.getMessage());
        }
    }

}
