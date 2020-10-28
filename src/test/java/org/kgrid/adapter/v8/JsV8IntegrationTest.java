package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Before;
import org.junit.Test;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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
    Executor executor = adapter.activate(URI.create("hello-world/"), null, endpointObject);
    Object helloResult = executor.execute("{\"name\":\"Bob\"}", "application/json");
    assertEquals("Hello, Bob", helloResult);
  }

  @Test
  public void testActivatesObjectWithArrayAndGetsExecutor() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("artifact-list-v1.0/deployment.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/bmicalc");
    Executor executor = adapter.activate(URI.create("artifact-list-v1.0/"), null, endpointObject);
    Object helloResult = executor.execute("{\"height\":2, \"weight\":80}", "application/json");
    assertEquals("20.0", helloResult);
  }

  @Test
  public void testActivatesObjectWithArrayWithMultipleElementsAndGetsExecutor() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("artifact-list-v2.0/deployment.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/bmicalc");
    Executor executor = adapter.activate(URI.create("artifact-list-v2.0/"), null, endpointObject);
    Object helloResult = executor.execute("{\"height\":2, \"weight\":80}", "application/json");
    assertEquals("20.0", helloResult);
  }

  @Test
  public void testActivatesObjectWithArrayWithMultipleElementsNoEntryAndGetsExecutor() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("artifact-list-v3.0/deployment.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/bmicalc");
    Executor executor = adapter.activate(URI.create("artifact-list-v3.0/"), null, endpointObject);
    Object helloResult = executor.execute("{\"height\":2, \"weight\":80}", "application/json");
    assertEquals("20.0", helloResult);
  }

  @Test
  public void testActivatesBundledJSObjectAndGetsExecutor() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("hello-world-v1.3/deploymentSpec.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/welcome");
    Executor executor = adapter.activate(URI.create("hello-world-v1.3/"), null, endpointObject);
    Object helloResult = executor.execute("{\"name\":\"Bob\"}", "application/json");
    assertEquals("Hello, Bob", helloResult);
  }

  @Test(expected = AdapterException.class)
  public void testCantCallOtherExecutor() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("hello-world/deploymentSpec.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/welcome");
    Executor helloExecutor = adapter.activate(URI.create("hello-world/"), null, endpointObject);
    activationContext.addExecutor("hello-world/welcome", helloExecutor);
    deploymentSpec = getDeploymentSpec("hello-exec/deploymentSpec.yaml");
    endpointObject = deploymentSpec.get("endpoints").get("/welcome");
    Executor executor = adapter.activate(URI.create("hello-exec/"), null, endpointObject);
    Object helloResult = executor.execute("{\"name\":\"Bob\"}", "application/json");
    assertEquals("Hello, Bob", helloResult);
  }

  private JsonNode getDeploymentSpec(String deploymentLocation) throws IOException {
    YAMLMapper yamlMapper = new YAMLMapper();
    ClassPathResource classPathResource = new ClassPathResource(deploymentLocation);
    JsonNode deploymentSpec =
        yamlMapper.readTree(classPathResource.getInputStream().readAllBytes());
    return deploymentSpec;
  }


  @Test
  public void testActivatesTestObjectAndGetsExecutor() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("v8-bmicalc-v1.0/deployment.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/bmicalc");
    Executor executor = adapter.activate(URI.create("v8-bmicalc-v1.0/"), null, endpointObject);
    Object bmiResult = executor.execute("{\"weight\":70, \"height\":1.70}", "application/json");
    assertEquals("24.2", bmiResult);
  }

  @Test
  public void testTestExecutiveObject() throws IOException {
    JsonNode deploymentSpec = getDeploymentSpec("hello-world/deploymentSpec.yaml");
    JsonNode endpointObject = deploymentSpec.get("endpoints").get("/welcome");
    Executor helloExecutor = adapter.activate(URI.create("hello-world/"), null, endpointObject);
    activationContext.addExecutor("hello-world/welcome", helloExecutor);

     deploymentSpec = getDeploymentSpec("v8-bmicalc-v1.0/deployment.yaml");
     endpointObject = deploymentSpec.get("endpoints").get("/bmicalc");
     helloExecutor = adapter.activate(URI.create("v8-bmicalc-v1.0/"), null, endpointObject);
    activationContext.addExecutor("v8-bmicalc-v1.0/bmicalc", helloExecutor);

    deploymentSpec = getDeploymentSpec("v8-executive-1.0.0/deployment.yaml");
    endpointObject = deploymentSpec.get("endpoints").get("/process");
    Executor executor = adapter.activate(URI.create("v8-executive-1.0.0/"), null, endpointObject);
    Object helloResult = executor.execute("{\"name\":\"Bob\", \"weight\":70, \"height\":1.70}", "application/json");
    assertEquals("{message: \"Hello, Bob\", bmi: \"24.2\"}",
            helloResult.toString()
    );
  }


}

class TestActivationContext implements ActivationContext {

  Map<String, Executor> executorMap = new HashMap<>();

  @Override
  public Executor getExecutor(String s) {
    return executorMap.get(s);
  }

  @Override
  public byte[] getBinary(URI s) {
    try {
      return new ClassPathResource(s.toString()).getInputStream().readAllBytes();
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

