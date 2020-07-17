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
        String bmiKoPackageName = "v8-bmicalc-v1.0";
        String bmiKoDeploymentSpecName = "deployment.yaml";
        String bmiKoEndpointName = "/bmicalc";
        Executor executor = getExecutor(bmiKoPackageName, bmiKoDeploymentSpecName, bmiKoEndpointName);
        Object bmiResult = executor.execute("{\"weight\":70, \"height\":1.70}");
        assertEquals("24.2", bmiResult);
    }

    @Test
    public void testSampleExecutiveObjectWithStringifiedInputObjects() throws IOException {
        String helloKoPackageName = "hello-world";
        String helloKoDeploymentSpecName = "deploymentSpec.yaml";
        String helloKoEndpointName = "/welcome";
        String bmiKoPackageName = "v8-bmicalc-v1.0";
        String bmiKoDeploymentSpecName = "deployment.yaml";
        String bmiKoEndpointName = "/bmicalc";
        String executiveKoPackageName = "v8-executive-1.0.0";
        String executiveKoDeploymentSpecName = "deployment.yaml";
        String executiveKoEndpointName = "/process";

        addKoToActivationContext(helloKoPackageName, helloKoDeploymentSpecName, helloKoEndpointName);
        addKoToActivationContext(bmiKoPackageName, bmiKoDeploymentSpecName, bmiKoEndpointName);
        Executor executor = getExecutor(executiveKoPackageName, executiveKoDeploymentSpecName, executiveKoEndpointName);

        Object helloResult = executor.execute("{\"name\":\"Bob\", \"weight\":70, \"height\":1.70}");
        assertEquals("{message: \"Hello, Bob\", bmi: \"24.2\"}",
                helloResult.toString()
        );
    }

    private void addKoToActivationContext(String packageName, String deploymentSpecName, String endpointName) throws IOException {
        JsonNode deploymentSpec = getDeploymentSpec(packageName + "/" + deploymentSpecName);
        JsonNode endpointObject = deploymentSpec.get("endpoints").get(endpointName);
        Executor helloExecutor = adapter.activate(packageName, "", "", endpointObject);
        activationContext.addExecutor(packageName + endpointName, helloExecutor);
    }

    private JsonNode getDeploymentSpec(String deploymentLocation) throws IOException {
        YAMLMapper yamlMapper = new YAMLMapper();
        ClassPathResource classPathResource = new ClassPathResource(deploymentLocation);
        JsonNode deploymentSpec =
                yamlMapper.readTree(classPathResource.getInputStream().readAllBytes());
        return deploymentSpec;
    }

    private Executor getExecutor(String packageName, String deploymentSpecName, String endpointName) throws IOException {
        JsonNode deploymentSpec = getDeploymentSpec(packageName + "/" + deploymentSpecName);
        JsonNode endpointObject = deploymentSpec.get("endpoints").get(endpointName);
        return adapter.activate(packageName, "", "", endpointObject);
    }
}

