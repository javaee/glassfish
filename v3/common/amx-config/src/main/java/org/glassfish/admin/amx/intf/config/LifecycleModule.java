package org.glassfish.admin.amx.intf.config;


public interface LifecycleModule {


    public String getClassName();

    public String getEnabled();

    public void setEnabled(String param1);

    public String getDescription();

    public void setDescription(String param1);

    public String getObjectType();

    public void setObjectType(String param1);

    public void setClassName(String param1);

    public String getClasspath();

    public void setClasspath(String param1);

    public String getLoadOrder();

    public void setLoadOrder(String param1);

    public String getIsFailureFatal();

    public void setIsFailureFatal(String param1);

}
