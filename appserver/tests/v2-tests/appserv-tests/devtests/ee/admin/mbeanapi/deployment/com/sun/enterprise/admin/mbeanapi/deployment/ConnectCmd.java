package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TLSParams;

/**
 */
public class ConnectCmd extends BaseCmd implements SourceCmd
{
    public static final String kHost        = "host";
    public static final String kPort        = "port";
    public static final String kUser        = "user";
    public static final String kPassword    = "password";
    public static final String kUseTLS      = "useTLS";

    public ConnectCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        if (isConnected())
        {
            return getConnectionSource();
        }

        final String    host        = (String)getCmdEnv().get(kHost);
        final Integer   port        = (Integer)getCmdEnv().get(kPort);
        final String    user        = (String)getCmdEnv().get(kUser);
        final String    password    = (String)getCmdEnv().get(kPassword);

        final AppserverConnectionSource cs = new AppserverConnectionSource(
            AppserverConnectionSource.PROTOCOL_RMI, host, port.intValue(), 
            user, password, getTLSParams(), null);

        getCmdEnv().put(kConnectionSource, cs);

        return cs;
    }
}
