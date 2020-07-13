package org.kgrid.adapter.v8;

import com.fasterxml.jackson.databind.JsonNode;
import org.graalvm.polyglot.Engine;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;

import java.nio.file.Path;

public class JsV8Adapter implements Adapter {

  Engine engine;
  ActivationContext context;

  @Override
  public String getType() {
    return null;
  }

  @Override
  public void initialize(ActivationContext context) {

    this.context = context;
    engine = Engine.newBuilder().build();
  }

  @Override
  public Executor activate(Path resource, String entry) {
    return new Executor() {
      @Override
      public Object execute(Object input) {
        return null;
      }
    };
  }

  @Override
  public Executor activate(String objectLocation, String arkId, String endpointName, JsonNode deploymentSpec) {
    return null;
  }


  @Override
  public String status() {
    if (engine == null) {
      return "DOWN";
    }
    return "UP";
  }
}
