package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
            .allowNativeAccess(true)
            .build();
    Value function;

    String artifact = deploymentSpec.get("artifact").asText();
    String artifactLocation = Paths.get(objectLocation, artifact).toString();

    try {
      byte[] src = activationContext.getBinary(artifactLocation);
      context.getBindings("js").putMember("context", activationContext);
      context.eval("js", new String(src));
      String functionName = deploymentSpec.get("function").asText();
      function = context.getBindings("js").getMember(functionName);
      if (function == null) {
        throw new AdapterException("Function " + functionName + " not found");
      }
    } catch (Exception e) {
      throw new AdapterException("Error loading source", e);
    }

    return new Executor() {
      @Override
      public Object execute(Object o) {
        try {
          Object input = replaceMaps(o);
          Value result = function.execute(input);
          return result.as(Object.class);
        } catch (PolyglotException e) {
          throw new AdapterException("Code execution error", e);
        }
      }
    };
  }

  public Object replaceMaps(Object o) {
    if (o instanceof Map) {
      ((Map) o)
          .forEach(
              (key, value) -> {
                if (value instanceof Map) {
                  ((Map) o).put(key, replaceMaps(value));
                }
              });
      return ProxyObject.fromMap((Map) o);
    }
    // TODO: Add array handling
    else {
      return o;
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
