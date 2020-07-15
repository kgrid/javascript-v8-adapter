package org.kgrid.adapter.v8;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.polyglot.HostAccess;
import org.junit.Before;
import org.junit.Test;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.springframework.core.io.ClassPathResource;

public class JsV8IntegrationTest {

  Adapter adapter;
  TestActivationContext activationContext;

  @Before
  public void setUp() {
    activationContext = new TestActivationContext();
    adapter = new JsV8Adapter();
    adapter.initialize(activationContext);
  }

  @Test
  public void testActivatesObjectAndGetsExecutor() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("hello-world/deploymentSpec.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/welcome");
    Executor executor = adapter.activate("hello-world", "", "", endpointObject);
    Object helloResult = executor.execute("\"Bob\"");
    assertEquals("Hello, Bob", helloResult);
  }

  @Test
  public void testCanCallOtherExecutor() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("hello-world/deploymentSpec.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/welcome");
    Executor helloExecutor = adapter.activate("hello-world", "", "", endpointObject);
    activationContext.addExecutor("hello-world/welcome", helloExecutor);
    deploymentSpec = getDeploymentSpec("hello-exec/deploymentSpec.yaml");
    endpointObject = deploymentSpec.get("endpoints").get("/welcome");
    Executor executor = adapter.activate("hello-exec", "", "", endpointObject);
    Object helloResult = executor.execute("\"Bob\"");
    assertEquals("Hello, Bob", helloResult);
  }

  private JsonNode getDeploymentSpec(String deploymentLocation) throws IOException {
    YAMLMapper yamlMapper = new YAMLMapper();
    ClassPathResource classPathResource = new ClassPathResource(deploymentLocation);
    JsonNode deploymentSpec =
        yamlMapper.readTree(classPathResource.getInputStream().readAllBytes());
    return deploymentSpec;
  }
}

class TestActivationContext implements ActivationContext {

  Map<String, Executor> executorMap = new HashMap<>();

  @Override
  public Executor getExecutor(String s) {
    return executorMap.get(s);
  }

  @Override
  public byte[] getBinary(String s) {
    try {
      return new ClassPathResource(s).getInputStream().readAllBytes();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getProperty(String s) {
    return null;
  }

  public void addExecutor(String id, Executor executor){
    executorMap.put(id, executor);
  }
}

