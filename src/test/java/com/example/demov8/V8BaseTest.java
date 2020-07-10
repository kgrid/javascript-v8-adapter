package com.example.demov8;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

public class V8BaseTest {

  @Test
  public void simpleScriptCompilesAndRuns() {

    Context context = Context.create();
    Value result = context.eval("js", "40+2");
    assertEquals(42,result.asInt());
  }

  @Test
  public void getsCompiledScript() throws IOException {
    Context context = Context.create("js");
    String jsCode = "function doStuff(p1, p2) {" +
            "return p1 + p2;" +
            "}";
    Source sourceCode = Source.newBuilder("js", jsCode, "jsCode.js").build();
    context.eval(sourceCode);
    Value function = context.getBindings("js").getMember("doStuff");
    Value result = function.execute(2, 5);
    assertEquals(result.asInt(), 7);
  }

  @Test
  public void initializeWithBinding() throws IOException {
    Context context = Context.create("js");
    String jsCode = "function doStuff(p1, p2) {" +
        "return p1 + p2 + thing;" +
        "}";
    context.getBindings("js").putMember("thing", 543);

    Source sourceCode = Source.newBuilder("js", jsCode, "jsCode.js").build();
    context.eval(sourceCode);
    Value function = context.getBindings("js").getMember("doStuff");
    Value result = function.execute(2, 5);
    assertEquals(550,result.asInt());
  }

  @Test
  public void addJavaObjectToBinding() throws IOException {
    Map valueStore = Collections.singletonMap("key",998);
    Context context = Context.newBuilder().allowHostAccess(HostAccess.ALL).build();
    String jsCode =
        "function doStuff(p1, p2) {" +
            "console.log(valueStore.get(\"key\"));" +
            "}";
    context.getBindings("js").putMember("valueStore", valueStore);

    Source sourceCode = Source.newBuilder("js", jsCode, "jsCode.js").build();
    context.eval(sourceCode);
    Value function = context.getBindings("js").getMember("doStuff");
    Value result = function.execute(2, 5);
  }

  @Test
  public void polyglotBindingTest() throws IOException {
    Map valueStore = Collections.singletonMap("key",998);
    Context context = Context.newBuilder()
        .allowPolyglotAccess(PolyglotAccess.ALL)
        .allowHostAccess(HostAccess.ALL)
        .build();
    String jsCode =
        "function doStuff(p1, p2) {" +
            "var v = Polyglot.import(\"valueStore\");" +
            "console.log(v.get(\"key\"));" +
            "return p1 + p2 + v.get(\"key\");" +
            "}";
    context.getPolyglotBindings().putMember("valueStore", valueStore);

    final Source js = Source.newBuilder("js", jsCode, "jsCode.js")
        .build();
    context.eval(js);

    Value function = context.getBindings("js").getMember("doStuff");
    assertEquals(1004, function.execute(3, 3).asInt());


    Context context2 = Context.newBuilder()
        .allowPolyglotAccess(
            PolyglotAccess.newBuilder().allowBindingsAccess("js").build())
        .build();

    context.eval(js);
    Value f2 = context.getBindings("js").getMember("doStuff");

    assertEquals(1006, f2.execute(4, 4).asInt());
  }
}

