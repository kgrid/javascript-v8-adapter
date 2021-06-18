package org.kgrid.adapter.v8;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.adapter.api.AdapterException;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
  public void executeThrowsAdapterExceptionWhenExecuteFails() {

    v8Executor = new V8Executor(null);
    try {
      v8Executor.execute(5, "text/plain");
      fail();
    } catch (AdapterException e) {
      assertEquals(null, e.getMessage());
    }
  }
}
