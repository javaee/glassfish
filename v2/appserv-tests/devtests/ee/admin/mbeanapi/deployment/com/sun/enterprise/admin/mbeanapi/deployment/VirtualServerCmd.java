package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Set;
import java.util.Map;

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.VirtualServerConfig;
import com.sun.appserv.management.config.HTTPServiceConfig;

/**
 */
public class VirtualServerCmd extends BaseCmd implements SourceCmd
{
    public static final String kCreateMode      = "CreateVirtualServer";
    public static final String kDeleteMode      = "DeleteVirtualServer";

    public static final String kName            = "Name";
    public static final String kHosts           = "Hosts";
    public static final String kConfigName      = "ConfigName";
    public static final String kOptional        = "Optional";

    private final String mode;

    public VirtualServerCmd(CmdEnv cmdEnv, String mode)
    {
        super(cmdEnv);
        this.mode = mode;
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        Object ret = new Integer(0);
        if (mode.equals(kCreateMode))
        {
            ret = create();
        }
        else if (mode.equals(kDeleteMode))
        {
            remove();
        }
        else
        {
            throw new Exception("Unknown mode");
        }
        return ret;
    }

    protected VirtualServerConfig create() throws Exception
    {
        final HTTPServiceConfig mgr = getHTTPServiceConfig(
            getConfigName());
        return mgr.createVirtualServerConfig(getName(), getHosts(), getOptional());
    }

    protected void remove() throws Exception
    {
        final String configName = getConfigName();
        final String vsName     = getName();

        getHTTPServiceConfig(configName).removeVirtualServerConfig(vsName);
    }

    private HTTPServiceConfig getHTTPServiceConfig(
        final String configName) throws Exception
    {
        final String j2eeTypeProp = Util.makeJ2EETypeProp(
                HTTPServiceConfig.J2EE_TYPE);
        final String configProp = Util.makeProp(
                ConfigConfig.J2EE_TYPE, configName);
        final String props = Util.concatenateProps(j2eeTypeProp, configProp);

        final Set s = getQueryMgr().queryPropsSet(props);
        assert s != null && s.size() == 1;

        return (HTTPServiceConfig)s.iterator().next();
    }

    private String getHosts()
    {
        return (String)getCmdEnv().get(kHosts);
    }

    private String getName()
    {
        return (String)getCmdEnv().get(kName);
    }

    private String getConfigName()
    {
        return (String)getCmdEnv().get(kConfigName);
    }

    private Map getOptional()
    {
        return (Map)getCmdEnv().get(kOptional);
    }
}
