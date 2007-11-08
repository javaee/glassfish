package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.VirtualServerConfigKeys;


/**
 */
public class CreateVirtualServerTest extends BaseTest
{
    private final Cmd target;

    static final String kName       = "myVirtualServer";
    static final String kConfigName = "server-config";
    static final String kHosts      = "${com.sun.aas.hostName}";
    static final String kState      = "on";
    static final String kDocRoot    = "${com.sun.aas.instanceRoot}/docroot";
    static final String kAccessLog  = "${com.sun.aas.instanceRoot}/logs/access";
    static final String kLogFile    = "${com.sun.aas.instanceRoot}/logs/server.log";
    static final String kHTTPListeners      = "http-listener-1";

    public CreateVirtualServerTest(final String user, 
        final String password, final String host, final int port, 
        final String vsName, final String configName, final String hosts)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final VirtualServerCmd createCmd = 
                cmdFactory.createVirtualServerCmd(vsName, configName,
                        hosts, getOptional(), VirtualServerCmd.kCreateMode);

        final PipeCmd p1 = new PipeCmd(connectCmd, createCmd);
        final PipeCmd p2 = new PipeCmd(p1, new VerifyCreateCmd());

        target = p2;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }


    public static void main(String[] args) throws Exception
    {
        new CreateVirtualServerTest(
                "admin", "password", "localhost", 8686, 
                kName, kConfigName, kHosts).run();
    }

    private Map getOptional()
    {
        final Map optional = new HashMap();
        optional.put(VirtualServerConfigKeys.STATE_KEY, kState);
        optional.put(VirtualServerConfigKeys.HTTP_LISTENERS_KEY, kHTTPListeners);
        //optional.put(VirtualServerConfigKeys.DOC_ROOT_KEY, kDocRoot);
        //optional.put(VirtualServerConfigKeys.LOG_FILE_KEY, kLogFile);
        //optional.put(VirtualServerConfigKeys.DOC_ROOT_PROPERTY_KEY, kDocRoot);
        //optional.put(VirtualServerConfigKeys.ACCESS_LOG_PROPERTY_KEY, kAccessLog);
        return optional;
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private VirtualServerConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (VirtualServerConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("Name="+res.getName());
            System.out.println("State="+res.getState());
            System.out.println("HTTPListeners="+res.getHTTPListeners());
            System.out.println("Hosts="+res.getHosts());
            //System.out.println("Docroot="+res.getDocroot());
            System.out.println("LogFile="+res.getLogFile());
            System.out.println("Doc root property="+res.getPropertyValue("docroot"));
            System.out.println("Access log property="+res.getPropertyValue("accesslog"));

            return new Integer(0);
        }

    }
}
