package com.sun.enterprise.admin.mbeanapi.deployment;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.QueryMgr;
import com.sun.appserv.management.j2ee.J2EEDomain;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TLSParams;

/**
 */
public abstract class BaseCmd implements Cmd, SinkCmd
{
    public static final String kConnectionSource = "connectionSource";

    private final CmdEnv _cmdEnv;

    protected BaseCmd(CmdEnv cmdEnv)
    {
        if (cmdEnv == null)
        {
            throw new IllegalArgumentException();
        }
        _cmdEnv = cmdEnv;
    }

    public void setPipedData(Object o)
    {
        if (o instanceof AppserverConnectionSource)
        {
            setConnectionSource((AppserverConnectionSource)o);
        }
        else
        {
            throw new IllegalArgumentException(
                "setPipedData: Support only AppserverConnectionSource for now");
        }
    }

    protected CmdEnv getCmdEnv()
    {
        return _cmdEnv;
    }

    protected boolean isConnected()
    {
        return _cmdEnv.get(kConnectionSource) != null;
    }

    protected AppserverConnectionSource getConnectionSource()
    {
        return (AppserverConnectionSource)_cmdEnv.get(kConnectionSource);
    }

    protected void setConnectionSource(AppserverConnectionSource cs)
    {
        _cmdEnv.put(kConnectionSource, cs);
    }

    protected final DomainRoot getDomainRoot() throws Exception
    {
        return getConnectionSource().getDomainRoot();
    }

    protected final DomainConfig getDomainConfig() throws Exception
    {
       return getDomainRoot().getDomainConfig(); 
    }

    protected final J2EEDomain getJ2EEDomain() throws Exception
    {
       return getDomainRoot().getJ2EEDomain(); 
    }

    protected final TLSParams getTLSParams()
    {
        return Env.useTLS() ? Env.getTLSParams() : null;
    }

    protected final QueryMgr getQueryMgr() throws Exception
    {
       return getDomainRoot().getQueryMgr(); 
    }
}
