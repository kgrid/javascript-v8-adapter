package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import org.graalvm.polyglot.*;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class JsV8Adapter implements Adapter {

    Engine engine;
    ActivationContext activationContext;

    final Logger log = LoggerFactory.getLogger(JsV8Adapter.class);

    @Override
    public List<String> getEngines() {
        return Collections.singletonList("javascript");
    }

    @Override
    public void initialize(ActivationContext context) {

        activationContext = context;
        engine = Engine.newBuilder().build();

        activationContext.refresh(getEngines().get(0));
    }

    @Override
    public Executor activate(URI absoluteLocation, URI endpointUri, JsonNode deploymentSpec) {

        Context context =
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.ALL)
                        .allowExperimentalOptions(true)
                        .option("js.experimental-foreign-object-prototype", "true")
                        .allowHostClassLookup(className -> true)
                        .allowNativeAccess(true)
                        .build();
        String artifact;
        final JsonNode artifactNode = deploymentSpec.get("artifact");
        if (artifactNode.isArray()) {
            if (artifactNode.size() > 1 && deploymentSpec.has("entry")) {
                artifact = deploymentSpec.get("entry").asText();
            } else {
                artifact = artifactNode.get(0).asText();
            }
        } else {
            artifact = artifactNode.asText();
        }
        try {
            URI artifactLocation = absoluteLocation.resolve(artifact);
            context.getBindings("js").putMember("context", activationContext);
            InputStream src = activationContext.getBinary(artifactLocation);
            String functionName = deploymentSpec.get("function").asText();
            context.eval("js", new String(src.readAllBytes()));
            return new V8Executor(context.getBindings("js").getMember(functionName));
        } catch (PolyglotException e) {
            String errorWithFilename = e.getMessage().replace("Unnamed", artifact);
            String errorMessage = errorWithFilename.substring(0, errorWithFilename.indexOf("\r\n"));
            log.error(errorWithFilename);
            throw new AdapterException("Error loading source. " + errorMessage, e);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new AdapterException("Error loading source. " + e.getMessage(), e);
        }
    }

    @Override
    public String status() {
        if (engine == null) {
            return "DOWN";
        }
        return "UP";
    }
}
