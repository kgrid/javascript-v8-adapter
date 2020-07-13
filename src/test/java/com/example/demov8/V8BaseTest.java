package com.example.demov8;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.polyglot.*;
import org.junit.jupiter.api.Test;

public class V8BaseTest {

  @Test
  public void simpleScriptCompilesAndRuns() {

    Context context = Context.create();
    Value result = context.eval("js", "40+2");
    assertEquals(42, result.asInt());
  }

  @Test
  public void getsCompiledScript() throws IOException {
    Context context = Context.newBuilder().allowHostAccess(true).build();
    String jsCode = "function doStuff(p1, p2) {" + "return p1 + p2;" + "}";
    Source sourceCode = Source.newBuilder("js", jsCode, "jsCode.js").build();
    context.eval(sourceCode);
    Value function = context.getBindings("js").getMember("doStuff");
    Value result = function.execute(2, 5);
    assertEquals(result.asInt(), 7);
  }

  @Test
  public void initializeWithBinding() throws IOException {
    Context context = Context.create("js");
    String jsCode = "function doStuff(p1, p2) {" + "return p1 + p2 + thing;" + "}";
    context.getBindings("js").putMember("thing", 543);

    Source sourceCode = Source.newBuilder("js", jsCode, "jsCode.js").build();
    context.eval(sourceCode);
    Value function = context.getBindings("js").getMember("doStuff");
    Value result = function.execute(2, 5);
    assertEquals(550, result.asInt());
  }

  @Test
  public void addJavaObjectToBinding() throws IOException {
    Map<String, Object> valueStore = Collections.singletonMap("key", 998);
    Context context = Context.newBuilder().allowHostAccess(HostAccess.ALL).build();
    String jsCode = "function doStuff(p1, p2) {" + "console.log(valueStore.get(\"key\"));" + "}";
    context.getBindings("js").putMember("valueStore", valueStore);

    Source sourceCode = Source.newBuilder("js", jsCode, "jsCode.js").build();
    context.eval(sourceCode);
    Value function = context.getBindings("js").getMember("doStuff");
    Value result = function.execute(2, 5);
  }

  @Test
  public void polyglotBindingTest() throws IOException {
    Engine engine = Engine.create();
    Map<String, Integer> activationContext = new HashMap<>();
    activationContext.put("key", 998);

    Context context =
        Context.newBuilder()
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .allowHostAccess(HostAccess.ALL)
            .engine(engine)
            .build();

    context.getPolyglotBindings().putMember("context", activationContext);
    context.eval("js", "var context = Polyglot.import(\"context\");");

    String jsCode =
        "function doStuff(p1, p2) {"
            + "console.log(context.get(\"key\"));"
            + "return p1 + p2 + context.get(\"key\");"
            + "}";

    final Source js = Source.newBuilder("js", jsCode, "jsCode.js").build();

    context.eval(js);

    Value function = context.getBindings("js").getMember("doStuff");
    assertEquals(1004, function.execute(3, 3).asInt());

  activationContext.put("key", 0);

    Context context2 =
        Context.newBuilder()
            .allowPolyglotAccess(PolyglotAccess.ALL)
            .allowHostAccess(HostAccess.ALL)
            .engine(engine)
            .build();
    context2.getPolyglotBindings().putMember("context", activationContext);
    context2.eval("js", "var context = Polyglot.import(\"context\");");
    context2.eval(js);
    Value function2 = context2.getBindings("js").getMember("doStuff");
    assertEquals(6, function2.execute(3, 3).asInt());
  }
}
