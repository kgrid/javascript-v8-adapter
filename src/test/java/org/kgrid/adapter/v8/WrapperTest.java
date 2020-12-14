package org.kgrid.adapter.v8;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WrapperTest {
  Adapter adapter;
  ObjectNode deploymentSpec;

  @Mock
  ActivationContext activationContext;
  private ObjectMapper mapper = new ObjectMapper();

  @Before
  public void setUp() {
    adapter = new JsV8Adapter();
    adapter.initialize(activationContext);
    mapper = new ObjectMapper();
    deploymentSpec = mapper.createObjectNode();
    deploymentSpec.put("function", "hello");
    deploymentSpec.put("artifact", "src/welcome.js");
  }

  @Test
  public void simpleObjectCallWorks() {
    Executor executor = getSimpleKoWithObjectInput(URI.create("hello-world/"));
    Object result = executor.execute("b", "text/plain");

    assertEquals("Hello, b-simple", result);
  }

  @Test
  public void testExecKoCallsKoWithPrimitiveInput() {
    Executor ex = getSimpleKoWithObjectInput(URI.create("hello-simple/"));
    when(activationContext.getExecutor("hello-simple")).thenReturn(ex);

    Executor executor = getExecKoWithObjectInput(URI.create("hello-exec/"));
    Object result = executor.execute("Bob", "text/plain");

    assertEquals("Exec: Hello, Bob-simple-exec", result);
  }

  private Executor getSimpleKoWithObjectInput(URI id) {
    when(activationContext.getBinary(id.resolve("index.js")))
        .thenReturn(new ByteArrayInputStream("function helloSimple(input){ return 'Hello, ' + input + '-simple';}".getBytes()));
    return adapter.activate(id, null,
        mapper.createObjectNode()
        .put("function", "helloSimple")
        .put("artifact", "index.js"));
  }

  private Executor getExecKoWithObjectInput(final URI id) {
    when(activationContext.getBinary(id.resolve("index.js")))
        .thenReturn(new ByteArrayInputStream(("function helloExec(input){ "
            + "var ex = context.getExecutor('hello-simple');"
            + "return 'Exec: ' + ex.execute(JSON.stringify(input), \"application/json\") + '-exec';"
            + "}").getBytes()));
    return adapter.activate(id, null,
        mapper.createObjectNode()
            .put("function", "helloExec")
            .put("artifact", "index.js")
    );
  }
}
