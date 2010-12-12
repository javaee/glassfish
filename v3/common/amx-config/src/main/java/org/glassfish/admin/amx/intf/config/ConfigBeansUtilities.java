package org.glassfish.admin.amx.intf.config;


import java.util.Map;


public interface ConfigBeansUtilities {


    public String toString(Throwable param1);

    public String getLocation(String param1);

    public String join(Iterable param1, String param2);

    public boolean toBoolean(String param1);

    public Map<String, Server> getServers();

    public String getDirectoryDeployed(String param1);

    public String getEnabled(String param1, String param2);

    public String getContextRoot(String param1);

    public String getLibraries(String param1);

    public String getVirtualServers(String param1, String param2);

    public String getDefaultFormat();

    public String getDefaultRotationPolicy();

    public String getDefaultRotationEnabled();

    public String getDefaultRotationIntervalInMinutes();

    public String getDefaultQueueSizeInBytes();

    public Map<String, Application> getSystemApplicationsReferencedFrom(String param1);

    public Application getSystemApplicationReferencedFrom(String param1, String param2);

    public boolean isNamedSystemApplicationReferencedFrom(String param1, String param2);

    public Server getServerNamed(String param1);

    public Map<String, Application> getAllDefinedSystemApplications();

    public Map<String, ApplicationRef> getApplicationRefsInServer(String param1);

    public Map<String, ApplicationRef> getApplicationRefsInServer(String param1, boolean param2);

    public ApplicationRef getApplicationRefInServer(String param1, String param2);

    public Domain getDomain();

}
