package org.kgrid.adapter.v8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JsV8ExecutorTest {

  Adapter adapter;
  ObjectNode deploymentSpec;

  @Mock ActivationContext activationContext;

  @Before
  public void setUp() {
    adapter = new JsV8Adapter();
    adapter.initialize(activationContext);
    deploymentSpec = new ObjectMapper().createObjectNode();
    deploymentSpec.put("function", "hello");
    deploymentSpec.put("artifact", "src/welcome.js");
    when(activationContext.getBinary(Paths.get("hello-world/src/welcome.js").toString()))
        .thenReturn("function hello(name){ return 'Hello, ' +name;}".getBytes());
  }

  @Test(expected = AdapterException.class)
  public void codeExecutionErrorThrowsAdapterException() {
    when(activationContext.getBinary(Paths.get("hello-world/src/welcome.js").toString()))
        .thenReturn("function hello(name){ throw 'Error!!, ' + name;}".getBytes());
    Executor ex = adapter.activate("hello-world", "", "", deploymentSpec);
    ex.execute("");
  }

  @Test
  public void canReadListObjectInput() {
    when(activationContext.getBinary(Paths.get("hello-world/src/welcome.js").toString()))
        .thenReturn(
            "function hello(name){ var bar = name[0]; return 'Hello, ' + bar; }".getBytes());
    Executor ex = adapter.activate("hello-world", "", "", deploymentSpec);
    assertEquals("Hello, Tom", ex.execute(Collections.singletonList("Tom")));
  }

  @Test
  public void canReadMapObjectInput() {
    when(activationContext.getBinary(Paths.get("hello-world/src/welcome.js").toString()))
        .thenReturn("function hello(name){ var bar = name.a; return 'Hello, ' + bar; }".getBytes());
    Executor ex = adapter.activate("hello-world", "", "", deploymentSpec);
    Map<String, String> lhm = new LinkedHashMap<>();
    lhm.put("a", "Tom");
    assertEquals("Hello, Tom", ex.execute(lhm));
  }

  @Test
  public void canReadNestedMapObjectInput() {
    when(activationContext.getBinary(Paths.get("hello-world/src/welcome.js").toString()))
        .thenReturn(
            "function hello(name){ var bar = name.map.a; return 'Hello, ' + bar; }".getBytes());
    Executor ex = adapter.activate("hello-world", "", "", deploymentSpec);
    Map<String, Map> lhm = new LinkedHashMap<>();
    Map<String, String> nested = new LinkedHashMap<>();
    nested.put("a", "Tom");
    lhm.put("map", nested);
    assertEquals("Hello, Tom", ex.execute(lhm));
  }

//    @Test  TODO: MAKE THIS WORK
    public void canReturnArrayFromJS() throws JsonProcessingException {
        when(activationContext.getBinary(Paths.get("hello-world/src/welcome.js").toString()))
                .thenReturn(
                        "function hello(name){ return [1, 2, 3];}".getBytes());
        Executor ex = adapter.activate("hello-world", "", "", deploymentSpec);
        Object result = ex.execute(0);
        System.out.println(result);
        String json = new ObjectMapper().writeValueAsString(result);
        assertEquals("[1, 2, 3]", json);
    }

    @Test
    public void canReturnMapFromJS() throws JsonProcessingException {
        when(activationContext.getBinary(Paths.get("hello-world/src/welcome.js").toString()))
                .thenReturn(
                        "function hello(name){ return {'a':'1', 'b':'2', 'c':'3'};}".getBytes());
        Executor ex = adapter.activate("hello-world", "", "", deploymentSpec);
        Object result = ex.execute(0);
        String json = new ObjectMapper().writeValueAsString(result);
        assertEquals("{\"a\":\"1\",\"b\":\"2\",\"c\":\"3\"}", json);
    }

//    @Test  This is also bad
    public void canReturnMapWithArrayFromJS() throws JsonProcessingException {
        when(activationContext.getBinary(Paths.get("hello-world/src/welcome.js").toString()))
                .thenReturn(
                        "function hello(name){ return {'a':[1, 4, 5], 'b':'2', 'c':'3'};}".getBytes());
        Executor ex = adapter.activate("hello-world", "", "", deploymentSpec);
        Object result = ex.execute(0);
        String json = new ObjectMapper().writeValueAsString(result);
        assertEquals("{\"a\":[1, 4, 5],\"b\":\"2\",\"c\":\"3\"}", json);
    }
}
