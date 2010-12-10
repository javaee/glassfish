package org.glassfish.admin.amx.intf.config;

public interface Node extends NamedConfigElement, PropertiesAccess, SystemPropertiesAccess {


    public String getName();

    public void setName(String param1);

    public String getType();

    public void setType(String param1);

    public String getNodeDir();

    public void setNodeDir(String param1);

    public String getNodeHost();

    public void setNodeHost(String param1);

    public String getInstallDir();

    public void setInstallDir(String param1);

    public SshConnector getSshConnector();

    public void setSshConnector(SshConnector param1);

    public String getInstallDirUnixStyle();

    public String getNodeDirUnixStyle();

    public String getNodeDirAbsolute();

    public String getNodeDirAbsoluteUnixStyle();

    public boolean nodeInUse();

    public boolean isLocal();

}
