package org.glassfish.admin.amx.intf.config;


public interface ConnectorModule {


    public String getLocation();

    public String getDirectoryDeployed();

    public String getEnabled();

    public void setEnabled(String param1);

    public String getDescription();

    public void setDescription(String param1);

    public String getObjectType();

    public void setObjectType(String param1);

    public void setLocation(String param1);

    public void setDirectoryDeployed(String param1);

}
