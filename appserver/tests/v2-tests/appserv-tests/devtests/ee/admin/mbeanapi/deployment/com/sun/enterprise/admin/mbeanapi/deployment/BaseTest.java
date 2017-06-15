package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

/**
 */
public abstract class BaseTest
{
    public BaseTest()
    {
    }

    public void run() throws Exception
    {
        setup();
        runInternal();
        dismantle();
    }

    protected abstract void runInternal() throws Exception;

    protected void setup() throws Exception {}
    protected void dismantle() throws Exception {}

    protected CmdFactory getCmdFactory()
    {
        return Env.getCmdFactory();
    }

}
