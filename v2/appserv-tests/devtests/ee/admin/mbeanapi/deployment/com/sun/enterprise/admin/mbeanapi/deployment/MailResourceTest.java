package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;
import java.util.HashMap;

import com.sun.appserv.management.config.MailResourceConfig;
import com.sun.appserv.management.config.MailResourceConfigKeys;


/**
 */
public class MailResourceTest extends BaseTest
{
    private final Cmd target;

    static final String kJNDIName       = "myMailResource";
    static final String kHost           = "mailHost";
    static final String kUser           = "abc";
    static final String kFrom           = "abc@sun.com";
    static final String kObjectType     = "user";
    static final Boolean kDebug         = new Boolean(true);
    static final String kStoreProtocol  = "imap";
    static final String kStoreProtocolClass     = "com.sun.mail.imap.IMAPStore";
    static final String kTransportProtocol      = "smtp";
    static final String kTransportProtocolClass = "com.sun.mail.smtp.SMTPTransport";

    public MailResourceTest(final String user, 
        final String password, final String host, final int port, 
        final String jndiName, final String rHost, 
        final String rUser, final String rFrom)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateMailResourceCmd createCmd = 
                cmdFactory.createCreateMailResourceCmd(jndiName,
                        rHost, rUser, rFrom, getOptional());

        final DeleteMailResourceCmd deleteCmd = 
                cmdFactory.createDeleteMailResourceCmd(jndiName);

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
        new MailResourceTest(
                "admin", "password", "localhost", 8686, 
                kJNDIName, kHost, kUser, kFrom).run();
    }

    private Map getOptional()
    {
        final Map optional = new HashMap();
        //optional.put(CreateResourceKeys.RESOURCE_OBJECT_TYPE_KEY, 
            //kObjectType);
        optional.put(MailResourceConfigKeys.DEBUG_KEY, kDebug);
        return optional;
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private MailResourceConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (MailResourceConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("JNDIName="+res.getJNDIName());
            System.out.println("Debug="+res.getDebug());
            System.out.println("ObjectType="+res.getObjectType());
            System.out.println("Host="+res.getHost());
            System.out.println("User="+res.getUser());
            System.out.println("From="+res.getFrom());
            System.out.println("StoreProtocol="+res.getStoreProtocol());
            System.out.println("StoreProtocolClass="+res.getStoreProtocolClass());
            System.out.println("TransportProtocol="+res.getTransportProtocol());
            System.out.println("TransportProtocolClass="+res.getTransportProtocolClass());

            return new Integer(0);
        }

    }
}
