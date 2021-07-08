package org.kgrid.adapter.v8;

import org.graalvm.polyglot.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.adapter.api.AdapterException;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class V8ExecutorTest {

  V8Executor v8Executor;

  @Test
  public void executeReturnsResult() {
    Context context =
        Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowExperimentalOptions(true)
            .option("js.experimental-foreign-object-prototype", "true")
            .allowHostClassLookup(className -> true)
            .allowNativeAccess(true)
            .build();
    String baseFunctionCode = "function baseFunction(input){return input+1}";
    context.eval("js", baseFunctionCode);
    Value baseFunction = context.getBindings("js").getMember("baseFunction");
    v8Executor = new V8Executor(baseFunction);
    int result = (int) v8Executor.execute(5, "text/plain");
    assertEquals(6, result);
  }

  @Test
  public void executeLoadsModules() throws Exception {
    Context context =
            Context.newBuilder("js")
                    .allowHostAccess(HostAccess.ALL)
                    .allowIO(true)
                    .allowPolyglotAccess(PolyglotAccess.newBuilder().build())
                    .allowExperimentalOptions(true)
                    .option("js.experimental-foreign-object-prototype", "true")
                    .allowHostClassLookup(className -> true)
                    .allowNativeAccess(true)
                    .build();
    String baseFunctionCode = "import {welcome} from 'src/test/resources/js-modules-v1.0/src/index.mjs';\n" +
            "welcome;";
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("name", "ted");
    inputs.put("num", 4);
    Value function = context.eval(Source.newBuilder("js",baseFunctionCode, "test.mjs").build());
    v8Executor = new V8Executor(function);
    Object result = v8Executor.execute(inputs, "application/json");
    assertEquals("Welcome to the knowledge grid ted 6", result);
  }

  @Test
  public void executeThrowsAdapterExceptionWhenExecuteFails() {

    v8Executor = new V8Executor(null);
    try {
      v8Executor.execute(5, "text/plain");
      fail();
    } catch (AdapterException e) {
      assertNull(e.getMessage());
    }
  }
}
