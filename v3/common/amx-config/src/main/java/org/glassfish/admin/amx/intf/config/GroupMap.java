package org.glassfish.admin.amx.intf.config;


public interface GroupMap extends NamedConfigElement, PropertiesAccess, SystemPropertiesAccess {


    public String getEisGroup();

    public void setEisGroup(String param1);

    public String getMappedGroup();

    public void setMappedGroup(String param1);

}
