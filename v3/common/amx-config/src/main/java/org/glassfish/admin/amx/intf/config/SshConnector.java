package org.glassfish.admin.amx.intf.config;

public interface SshConnector {


    public String getSshPort();

    public void setSshPort(String param1);

    public String getSshHost();

    public void setSshHost(String param1);

    public SshAuth getSshAuth();

    public void setSshAuth(SshAuth param1);

}
