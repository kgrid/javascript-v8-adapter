package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
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
    Context context = Context.newBuilder().build();
    Value function;

    String artifact = deploymentSpec.get("artifact").asText();
    String artifactLocation = Paths.get(objectLocation, artifact).toString();

    try {
      byte[] src = activationContext.getBinary(artifactLocation);
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
        return function.execute(o).as(Object.class);
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
