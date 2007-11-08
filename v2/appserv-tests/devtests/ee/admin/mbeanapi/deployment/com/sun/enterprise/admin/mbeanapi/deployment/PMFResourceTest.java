package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfig;
import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfigKeys;


/**
 */
public class PMFResourceTest extends BaseTest
{
    private final Cmd target;

    static final String kJNDIName       = "myPMFResource";
    static final String kJDBCResourceJNDIName = "jndi/myJdbcResource";
    static final String kFactoryClass   = "a.b.c";
    static final String kObjectType     = "user";

    public PMFResourceTest(final String user, 
        final String password, final String host, final int port, 
        final String jndiName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreatePMFResourceCmd createCmd = 
                cmdFactory.createCreatePMFResourceCmd(jndiName, getOptional());

        final DeletePMFResourceCmd deleteCmd = 
                cmdFactory.createDeletePMFResourceCmd(jndiName);

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
        new PMFResourceTest("admin", "password", "localhost", 8686, 
                kJNDIName).run(); 
    }

    private Map getOptional()
    {
        final Map optional = new HashMap();
        //optional.put(CreateResourceKeys.RESOURCE_OBJECT_TYPE_KEY, 
                //kObjectType);
        optional.put(PersistenceManagerFactoryResourceConfigKeys.JDBC_RESOURCE_JNDI_NAME_KEY, 
                kJDBCResourceJNDIName);
        optional.put(PersistenceManagerFactoryResourceConfigKeys.FACTORY_CLASS_KEY, 
                kFactoryClass);
        return optional;
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private PersistenceManagerFactoryResourceConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (PersistenceManagerFactoryResourceConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("JNDIName="+res.getJNDIName());
            System.out.println("FactoryClass="+res.getFactoryClass());
            System.out.println("ObjectType="+res.getObjectType());
            System.out.println("JDBCResourceJNDIName="+res.getJDBCResourceJNDIName());
            System.out.println("Enabled="+res.getEnabled());

            return new Integer(0);
        }

    }
}
