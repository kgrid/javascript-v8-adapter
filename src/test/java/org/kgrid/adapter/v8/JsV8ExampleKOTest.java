package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Before;
import org.junit.Test;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URI;

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
        URI bmiKoPackageName = URI.create("v8-bmicalc-v1.0/");
        String bmiKoDeploymentSpecName = "deployment.yaml";
        String bmiKoEndpointName = "/bmicalc";
        Executor executor = getExecutor(bmiKoPackageName, bmiKoDeploymentSpecName, bmiKoEndpointName);
        Object bmiResult = executor.execute("{\"weight\":70, \"height\":1.70}", "application/json");
        assertEquals("24.2", bmiResult);
    }

    @Test
    public void testSampleObjectWithArtifactList() throws IOException {
        URI bmiKoPackageName = URI.create("artifact-list-v1.0/");
        String bmiKoDeploymentSpecName = "deployment.yaml";
        String bmiKoEndpointName = "/bmicalc";
        Executor executor = getExecutor(bmiKoPackageName, bmiKoDeploymentSpecName, bmiKoEndpointName);
        Object bmiResult = executor.execute("{\"weight\":70, \"height\":1.70}", "application/json");
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

        Object helloResult = executiveExecutor.execute("{\"name\":\"Bob\", \"weight\":70, \"height\":1.70}", "application/json");
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
        JsonNode deploymentSpec =
                yamlMapper.readTree(classPathResource.getInputStream().readAllBytes());
        return deploymentSpec;
    }

    private Executor getExecutor(URI packageName, String deploymentSpecName, String endpointName) throws IOException {
        JsonNode deploymentSpec = getDeploymentSpec(packageName.resolve(deploymentSpecName));
        JsonNode endpointObject = deploymentSpec.get("endpoints").get(endpointName);
        return adapter.activate(packageName, null, endpointObject);
    }
}

