package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import org.graalvm.polyglot.*;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;

import java.nio.file.Path;
import java.nio.file.Paths;

public class JsV8Adapter implements Adapter {

  Engine engine;
  ActivationContext activationContext;

  @Override
  public String getType() {
    return "JAVASCRIPT";
  }

  @Override
  public void initialize(ActivationContext context) {

    this.activationContext = context;
    engine = Engine.newBuilder().build();
  }

  @Override
  @Deprecated
  public Executor activate(Path resource, String entry) {
    return null;
  }

  @Override
  public Executor activate(
      String objectLocation, String arkId, String endpointName, JsonNode deploymentSpec) {

    // Might need to wrap in try with resources to have context close on failure
    Context context =
        Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowExperimentalOptions(true)
            .option("js.experimental-foreign-object-prototype", "true")
            .allowHostClassLookup(className -> true)
            .allowNativeAccess(true)
            .build();
    Value function;

    String artifact = deploymentSpec.get("artifact").asText();
    String artifactLocation = Paths.get(objectLocation, artifact).toString();

    try {
      byte[] src = activationContext.getBinary(artifactLocation);
      String functionName = deploymentSpec.get("function").asText();
      context.getBindings("js").putMember("context", activationContext);
      final String parseIO =
          "function parseInAndOut(input) {"
              + "let parsed;"
              + "try {"
              + "parsed = JSON.parse(input);"
              + "} catch (error) {"
              + "return "
              + functionName
              + "(input);"
              + "}"
              + "return "
              + functionName
              + "(parsed);"
              + "}";
      context.eval("js", parseIO);
      context.eval("js", new String(src));
      function = context.getBindings("js").getMember("parseInAndOut");
//      function = context.getBindings("js").getMember(functionName);
    } catch (Exception e) {
      throw new AdapterException("Error loading source", e);
    }

    return new Executor() {
      @Override
      public Object execute(Object input) {
        try {
          Value result = function.execute(input);
          return result.as(Object.class);
        } catch (PolyglotException e) {
          throw new AdapterException("Code execution error", e);
        }
      }
    };
  }

  @Override
  public String status() {
    if (engine == null) {
      return "DOWN";
    }
    return "UP";
  }
}
