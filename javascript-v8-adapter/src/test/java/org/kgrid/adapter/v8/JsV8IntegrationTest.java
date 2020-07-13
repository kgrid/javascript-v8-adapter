package org.kgrid.adapter.v8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.junit.Before;
import org.junit.Test;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import static org.junit.Assert.assertEquals;

public class JsV8IntegrationTest {

    Adapter adapter;
    ObjectNode deploymentSpec;
    ActivationContext activationContext;

    @Before
    public void setUp() throws IOException {
        activationContext = new testActivationContext();
        adapter = new JsV8Adapter();
        adapter.initialize(activationContext);
        YAMLMapper yamlMapper = new YAMLMapper();
        ClassPathResource classPathResource = new ClassPathResource("hello-world/deploymentSpec.yaml");
        JsonNode jsonNode = yamlMapper.readTree(classPathResource.getInputStream().readAllBytes());
    }

    @Test
    public void testActivatesObjectAndGetsExecutor() throws JsonProcessingException {
        Executor executor = adapter.activate("hello-world", "", "", deploymentSpec);
        Object helloResult = executor.execute("Bob");
        String result = new ObjectMapper().writeValueAsString(helloResult);
        assertEquals("\"Hello, Bob\"", result);
    }

    @Test
    public void testLoadsDeploymentSpec(){
        Executor executor = adapter.activate("hello-world", "", "", deploymentSpec);

    }

}

class testActivationContext implements ActivationContext{
@Override
public Executor getExecutor(String s) {
        return null;
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
        }