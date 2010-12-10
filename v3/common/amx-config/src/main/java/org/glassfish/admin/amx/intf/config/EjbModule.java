package org.glassfish.admin.amx.intf.config;

import java.util.Map;

public interface EjbModule {


    public String getLocation();

    public String getDirectoryDeployed();

    public String getEnabled();

    public void setEnabled(String param1);

    public String getDescription();

    public void setDescription(String param1);

    public String getObjectType();

    public void setObjectType(String param1);

    public String getAvailabilityEnabled();

    public void setAvailabilityEnabled(String param1);

    public Map<String, WebServiceEndpoint> getWebServiceEndpoint();

    public String getLibraries();

    public void setLibraries(String param1);

    public void setLocation(String param1);

    public void setDirectoryDeployed(String param1);

}
