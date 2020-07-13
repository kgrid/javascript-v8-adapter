package org.kgrid.adapter.v8;

import junit.framework.TestCase;
import org.junit.Test;
import org.kgrid.adapter.api.ActivationContext;
import org.kgrid.adapter.api.Adapter;
import org.kgrid.adapter.api.Executor;

import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsV8AdapterTest {

    @Test
    public void statusIsDownNoEngine() {
        Adapter adapter = new JsV8Adapter();
        assertEquals("DOWN", adapter.status());
    }

    @Test
    public void initializeSucceeds() {
        Adapter adapter = new JsV8Adapter();
        adapter.initialize(new ActivationContext() {
            @Override
            public Executor getExecutor(String key) {
                return null;
            }

            @Override
            public byte[] getBinary(String pathToBinary) {
                return new byte[0];
            }

            @Override
            public String getProperty(String key) {
                return null;
            }
        });
        assertEquals("UP", adapter.status());
    }

    @Test
    public void activationHasAccessToContext(){
        Adapter adapter = new JsV8Adapter();
        Executor ex = adapter.activate(Paths.get(""), "");
        assertNotNull(ex);
    }
}