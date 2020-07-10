package com.example.demov8;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class V8BaseTest {

  @Test
  public void simpleScriptCompilesAndRuns() {

    Context context = Context.create();
    Value result = context.eval("js", "40+2");
    assertEquals(42,result.asInt());
  }
}
