package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.config.CustomResourceConfig;

/**
 */
public class CustomResourceTest extends BaseTest
{
    private final Cmd target;

    static final String kJNDIName       = "myCustomResource";
    static final String kResType        = "abc";
    static final String kFactoryClass   = "a.b.c";
    static final String kObjectType     = "user";

    public CustomResourceTest(final String user, 
        final String password, final String host, final int port, 
        final String jndiName, final String resType, final String factoryClass)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateCustomResourceCmd createCmd = 
                cmdFactory.createCreateCustomResourceCmd(jndiName, 
                        resType, factoryClass, getOptional());

        final DeleteCustomResourceCmd deleteCmd = 
                cmdFactory.createDeleteCustomResourceCmd(jndiName);

        final PipeCmd p1 = new PipeCmd(connectCmd, createCmd);
        final PipeCmd p2 = new PipeCmd(p1, new VerifyCreateCmd());
        final PipeCmd p3 = new PipeCmd(connectCmd, deleteCmd);

        final CmdChainCmd chainCmd = new CmdChainCmd();
        chainCmd.addCmd(p2);
        chainCmd.addCmd(p3);

        target = chainCmd;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }


    public static void main(String[] args) throws Exception
    {
        new CustomResourceTest("admin", "password", "localhost", 8686, 
                kJNDIName, kResType, kFactoryClass).run(); 
    }

    private Map getOptional()
    {
        final Map optional = new HashMap();
        //optional.put(CreateResourceKeys.RESOURCE_OBJECT_TYPE_KEY, 
                //kObjectType);
        return optional;
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private CustomResourceConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (CustomResourceConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("JNDIName="+res.getJNDIName());
            System.out.println("ResType="+res.getResType());
            System.out.println("ObjectType="+res.getObjectType());
            System.out.println("FactoryClass="+res.getFactoryClass());
            System.out.println("Enabled="+res.getEnabled());

            return new Integer(0);
        }

    }
}
