package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsV8ExampleKOTest {

    Adapter adapter;
    TestActivationContext activationContext;

    @BeforeEach
    public void setUp() {
        activationContext = new TestActivationContext();
        adapter = new JsV8Adapter();
        adapter.initialize(activationContext);
    }

    @Test
    public void testSampleSimpleObject() throws IOException {
        URI bmiKoPackageName = URI.create("v8-bmicalc-v1.0/");
        String bmiKoDeploymentSpecName = "deployment.yaml";
        String bmiKoEndpointName = "/bmicalc";
        Executor executor = getExecutor(bmiKoPackageName, bmiKoDeploymentSpecName, bmiKoEndpointName);
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("height", 1.70);
        inputs.put("weight", 70);
        Object bmiResult = executor.execute(inputs, "application/json");
        assertEquals("24.2", bmiResult);
    }

    @Test
    public void testSampleObjectWithArtifactList() throws IOException {
        URI bmiKoPackageName = URI.create("artifact-list-v1.0/");
        String bmiKoDeploymentSpecName = "deployment.yaml";
        String bmiKoEndpointName = "/bmicalc";
        Executor executor = getExecutor(bmiKoPackageName, bmiKoDeploymentSpecName, bmiKoEndpointName);
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("height", 1.70);
        inputs.put("weight", 70);
        Object bmiResult = executor.execute(inputs, "application/json");
        assertEquals("24.2", bmiResult);
    }

    @Test
    public void testSampleExecutiveObjectWithStringifiedInputObjects() throws IOException {
        URI helloKoPackageName = URI.create("hello-world/");
        String helloKoDeploymentSpecName = "deploymentSpec.yaml";
        String helloKoEndpointName = "/welcome";
        URI bmiKoPackageName = URI.create("v8-bmicalc-v1.0/");
        String bmiKoDeploymentSpecName = "deployment.yaml";
        String bmiKoEndpointName = "/bmicalc";
        URI executiveKoPackageName = URI.create("v8-executive-1.0.0/");
        String executiveKoDeploymentSpecName = "deployment.yaml";
        String executiveKoEndpointName = "/process";

        addKoToActivationContext(helloKoPackageName, helloKoDeploymentSpecName, helloKoEndpointName);
        addKoToActivationContext(bmiKoPackageName, bmiKoDeploymentSpecName, bmiKoEndpointName);
        Executor executiveExecutor = getExecutor(executiveKoPackageName, executiveKoDeploymentSpecName, executiveKoEndpointName);
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("name", "Bob");
        inputs.put("height", 1.70);
        inputs.put("weight", 70);
        Object helloResult = executiveExecutor.execute(inputs, "application/json");
        assertEquals("{message: \"Hello, Bob\", bmi: \"24.2\"}",
                helloResult.toString()
        );
    }

    private void addKoToActivationContext(URI packageName, String deploymentSpecName, String endpointName) throws IOException {
        JsonNode deploymentSpec = getDeploymentSpec(packageName.resolve(deploymentSpecName));
        JsonNode endpointObject = deploymentSpec.get("endpoints").get(endpointName);
        Executor helloExecutor = adapter.activate(packageName, null, endpointObject);
        activationContext.addExecutor(packageName.resolve(endpointName.substring(1)).toString(), helloExecutor);
    }

    private JsonNode getDeploymentSpec(URI deploymentLocation) throws IOException {
        YAMLMapper yamlMapper = new YAMLMapper();
        ClassPathResource classPathResource = new ClassPathResource(deploymentLocation.toString());
        return yamlMapper.readTree(classPathResource.getInputStream().readAllBytes());
    }

    private Executor getExecutor(URI packageName, String deploymentSpecName, String endpointName) throws IOException {
        JsonNode deploymentSpec = getDeploymentSpec(packageName.resolve(deploymentSpecName));
        JsonNode endpointObject = deploymentSpec.get("endpoints").get(endpointName);
        return adapter.activate(packageName, null, endpointObject);
    }
}

