package org.glassfish.admin.amx.intf.config;


public interface SshAuth extends NamedConfigElement, PropertiesAccess, SystemPropertiesAccess {


    public String getUserName();

    public String getPassword();

    public void setUserName(String param1);

    public void setPassword(String param1);

    public String getKeyfile();

    public void setKeyfile(String param1);

    public String getKeyPassphrase();

    public void setKeyPassphrase(String param1);

}
