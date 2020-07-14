package org.kgrid.adapter.v8;

import org.graalvm.polyglot.proxy.ProxyObject;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ReplaceMapTest {

  JsV8Adapter adapter;

  @Before
  public void setUp() {
    adapter = new JsV8Adapter();
  }

  @Test
  public void primitiveIsReturned() {
    assertEquals(5, adapter.replaceMaps(5));
  }

  @Test
  public void replaceTopLevelMap() {

    Map<String, Object> o = Collections.singletonMap("a", "b");
    Object result = adapter.replaceMaps(o);
    assertTrue(result instanceof ProxyObject);
    assertTrue(((ProxyObject)result).hasMember("a"));
  }

  @Test
  public void replaceNestedMap() {
    Map<String, Object> topMap = new LinkedHashMap();
    Map<String, Object> childMap = new LinkedHashMap();
    topMap.put("c", childMap);
    childMap.put("a", "b");
    Object result = adapter.replaceMaps(topMap);
    assertTrue(((ProxyObject)result).hasMember("c"));
    ProxyObject childProxy = (ProxyObject)((ProxyObject) result).getMember("c");
    assertTrue(childProxy.hasMember("a"));
  }

//  @Test TODO: Make work with array
  public void replaceMapsInArray() {
    Map<String, Object>[] arrayOfMaps = new LinkedHashMap[2];
    Map<String, Object> childMap = new LinkedHashMap();
    Map<String, Object> littleChildMap = new LinkedHashMap();
    arrayOfMaps[0] = childMap;
    childMap.put("a", "b");
    arrayOfMaps[1] = littleChildMap;
    littleChildMap.put("c", "d");
    Object result = adapter.replaceMaps(arrayOfMaps);
    System.out.println(result);

    assertTrue(((ProxyObject)result).hasMember("c"));
    ProxyObject childProxy = (ProxyObject)((ProxyObject) result).getMember("c");
    assertTrue(childProxy.hasMember("a"));
  }

}
