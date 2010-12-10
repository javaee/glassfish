package org.glassfish.admin.amx.intf.config;

import java.util.Map;

public interface WorkSecurityMap {


    public String getEnabled();

    public void setEnabled(String param1);

    public String getDescription();

    public void setDescription(String param1);

    public String getResourceAdapterName();

    public void setResourceAdapterName(String param1);

    public Map<String, GroupMap> getGroupMap();

    public Map<String, PrincipalMap> getPrincipalMap();

}
