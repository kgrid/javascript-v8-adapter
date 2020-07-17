package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Before;
import org.junit.Test;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JsV8ExampleKOTest {

    Adapter adapter;
    TestActivationContext activationContext;

    @Before
    public void setUp() {
        activationContext = new TestActivationContext();
        adapter = new JsV8Adapter();
        adapter.initialize(activationContext);
    }

    @Test
    public void testSampleSimpleObject() throws IOException {
        JsonNode deploymentSpec = getDeploymentSpec("v8-bmicalc-v1.0/deployment.yaml");
        JsonNode endpointObject = deploymentSpec.get("endpoints").get("/bmicalc");
        Executor executor = adapter.activate("v8-bmicalc-v1.0", "", "", endpointObject);
        Object bmiResult = executor.execute("{\"weight\":70, \"height\":1.70}");
        assertEquals("24.2", bmiResult);
    }

    @Test
    public void testSampleExecutiveObjectWithStringifiedInputObjects() throws IOException {
        JsonNode deploymentSpec = getDeploymentSpec("hello-world/deploymentSpec.yaml");
        JsonNode endpointObject = deploymentSpec.get("endpoints").get("/welcome");
        Executor helloExecutor = adapter.activate("hello-world", "", "", endpointObject);
        activationContext.addExecutor("hello-world/welcome", helloExecutor);

        deploymentSpec = getDeploymentSpec("v8-bmicalc-v1.0/deployment.yaml");
        endpointObject = deploymentSpec.get("endpoints").get("/bmicalc");
        helloExecutor = adapter.activate("v8-bmicalc-v1.0", "", "", endpointObject);
        activationContext.addExecutor("v8-bmicalc-v1.0/bmicalc", helloExecutor);

        deploymentSpec = getDeploymentSpec("v8-executive-1.0.0/deployment.yaml");
        endpointObject = deploymentSpec.get("endpoints").get("/process");
        Executor executor = adapter.activate("v8-executive-1.0.0", "", "", endpointObject);
        Object helloResult = executor.execute("{\"name\":\"Bob\", \"weight\":70, \"height\":1.70}");
        assertEquals("{message: \"Hello, Bob\", bmi: \"24.2\"}",
                helloResult.toString()
        );
    }

    private JsonNode getDeploymentSpec(String deploymentLocation) throws IOException {
        YAMLMapper yamlMapper = new YAMLMapper();
        ClassPathResource classPathResource = new ClassPathResource(deploymentLocation);
        JsonNode deploymentSpec =
                yamlMapper.readTree(classPathResource.getInputStream().readAllBytes());
        return deploymentSpec;
    }


}

