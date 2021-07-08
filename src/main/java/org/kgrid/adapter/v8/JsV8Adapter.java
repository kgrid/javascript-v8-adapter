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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, String> options = new HashMap<>();
        options.put("js.experimental-foreign-object-prototype", "true");
        Context context =
                Context.newBuilder("js")
                        .allowHostAccess(HostAccess.ALL)
                        .allowIO(true)
                        .allowPolyglotAccess(PolyglotAccess.newBuilder().build())
                        .allowExperimentalOptions(true)
                        .options(options)
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
            String functionName = deploymentSpec.get("function").asText();
            if (artifact.endsWith(".mjs")) {
                String srcLocation = getSrcLocation(artifactLocation);
                String baseFunctionCode = String.format("import {%s} from '%s';\n" +
                        "%s;", functionName, srcLocation, functionName);
                Value function = context.eval(Source.newBuilder("js", baseFunctionCode, artifact).build());
                return new V8Executor(function);
            } else {
                context.getBindings("js").putMember("context", activationContext);
                InputStream src = activationContext.getBinary(artifactLocation);
                context.eval(Source.newBuilder("js", new String(src.readAllBytes()), artifactLocation.toString()).build());
                return new V8Executor(context.getBindings("js").getMember(functionName));
            }
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

    private String getSrcLocation(URI artifactLocation) {
        String shelfLocation = activationContext.getProperty("kgrid.shelf.cdostore.url");
        if(shelfLocation == null) {
            return artifactLocation.toString();
        }
        if (!shelfLocation.endsWith("/")) {
            shelfLocation += "/";
        }
        URI importUri = URI.create(shelfLocation.substring(shelfLocation.indexOf(':') + 1)).resolve(artifactLocation);
        return importUri.getHost() == null ? importUri.getPath() : importUri.getHost() + importUri.getPath();
    }

    @Override
    public String status() {
        if (engine == null) {
            return "DOWN";
        }
        return "UP";
    }
}
