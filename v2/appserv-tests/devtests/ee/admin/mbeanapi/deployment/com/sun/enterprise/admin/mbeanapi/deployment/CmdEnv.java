package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 */
public final class CmdEnv
{
    private final Map env = Collections.synchronizedMap(new HashMap(4));

    public CmdEnv()
    {
    }

    public Object get(Object key)
    {
        return env.get(key);
    }

    public void put(Object key, Object value)
    {
        env.put(key, value);
    }
}
