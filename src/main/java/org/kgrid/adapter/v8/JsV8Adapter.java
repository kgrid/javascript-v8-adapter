package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.AdapterException;
import org.kgrid.adapter.api.Executor;

import java.net.URI;

public class JsV8Adapter implements Adapter {

  Engine engine;
  ActivationContext activationContext;

  @Override
  public String getType() {
    return "V8";
  }

  @Override
  public void initialize(ActivationContext context) {

    activationContext = context;
    engine = Engine.newBuilder().build();
  }

  @Override
  public Executor activate(
      URI objectLocation,
      String naan,
      String name,
      String version,
      String endpointName,
      JsonNode deploymentSpec) {

    Context context =
        Context.newBuilder("js")
            .allowHostAccess(HostAccess.ALL)
            .allowExperimentalOptions(true)
            .option("js.experimental-foreign-object-prototype", "true")
            .allowHostClassLookup(className -> true)
            .allowNativeAccess(true)
            .build();
    try {
      String artifact = deploymentSpec.get("artifact").asText();
      URI artifactLocation = objectLocation.resolve(artifact);
      context.getBindings("js").putMember("context", activationContext);
      byte[] src = activationContext.getBinary(artifactLocation);
      String functionName = deploymentSpec.get("function").asText();
      context.eval("js", new String(src));
      return new V8Executor(
              createWrapperFunction(context), context.getBindings("js").getMember(functionName));
    } catch (Exception e) {
      throw new AdapterException("Error loading source", e);
    }
  }

  @Override
  public String status() {
    if (engine == null) {
      return "DOWN";
    }
    return "UP";
  }

  private Value createWrapperFunction(Context context) {
    context.eval(
        "js",
        "function wrapper(baseFunction, arg) { "
            + "let parsedArg;"
            + "try {"
            + "   parsedArg = JSON.parse(arg);"
            + "} catch (error) {"
            + "   return baseFunction(arg);"
            + "}"
            + "return baseFunction(parsedArg);"
            + "}");
    return context.getBindings("js").getMember("wrapper");
  }
}
