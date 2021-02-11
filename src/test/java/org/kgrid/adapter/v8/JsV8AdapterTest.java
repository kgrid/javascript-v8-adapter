package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JsV8AdapterTest {

  Adapter adapter;
  ObjectNode deploymentSpec;

  @Mock ActivationContext activationContext;

  @BeforeEach
  public void setUp() {
    adapter = new JsV8Adapter();
    adapter.initialize(activationContext);
    deploymentSpec = new ObjectMapper().createObjectNode();
    deploymentSpec.put("function", "hello");
    deploymentSpec.put("artifact", "src/welcome.js");
  }

  @Test
  public void statusIsDownNoEngine() {
    adapter = new JsV8Adapter();
    assertEquals("DOWN", adapter.status());
  }

  @Test
  public void statusIsUpWhenInitializedWithActivationContext() {
    adapter.initialize(
        new ActivationContext() {
          @Override
          public Executor getExecutor(String key) {
            return null;
          }

          @Override
          public InputStream getBinary(URI pathToBinary) {
            return null;
          }

          @Override
          public String getProperty(String key) {
            return null;
          }

          @Override
          public void reactivate(String s) {

          }
        });
    assertEquals("UP", adapter.status());
  }

  @Test
  public void badArtifactThrowsGoodError() {
    RuntimeException runtimeException =
        new RuntimeException("Binary resource not found src/tolkien.js");
    when(activationContext.getBinary(URI.create("hello-world/src/tolkien.js")))
        .thenThrow(runtimeException);
    deploymentSpec.put("artifact", "src/tolkien.js");

    Exception ex =
        assertThrows(
            AdapterException.class,
            () -> adapter.activate(URI.create("hello-world/"), null, deploymentSpec));
    assertAll(
        () ->
            assertEquals(
                "Error loading source. Binary resource not found src/tolkien.js", ex.getMessage()),
        () -> assertEquals(runtimeException, ex.getCause()));
  }

  @Test
  public void throwsGoodErrorWhenActivateCantFindFunction() {
    deploymentSpec.put("function", "goodbye1");
    when(activationContext.getBinary(any(URI.class)))
        .thenReturn(
            new ByteArrayInputStream(
                "function goodbye(name){ return 'Goodbye, ' + name;}".getBytes()));
    try {
      adapter.activate(URI.create("hello-world/"), null, deploymentSpec);
    } catch (Exception ex) {
      assertEquals("Error loading source", ex.getMessage());
      assertEquals("Function goodbye1 not found", ex.getCause().getMessage());
    }
  }

  @Test
  public void worksWithListOfArtifacts() {

    deploymentSpec.set("artifact", new ObjectMapper().createArrayNode().add("src/tolkien.js"));
  }

  @Test
  public void getTypeReturnsV8() {
    assertEquals(Collections.singletonList("javascript"), adapter.getEngines());
  }
}
