package org.glassfish.admin.amx.intf.config;


public interface AppclientModule {


    public String getLocation();

    public String getDirectoryDeployed();

    public String getDescription();

    public void setDescription(String param1);

    public String getJavaWebStartEnabled();

    public void setJavaWebStartEnabled(String param1);

    public void setLocation(String param1);

    public void setDirectoryDeployed(String param1);

}
