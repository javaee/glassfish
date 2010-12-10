package org.glassfish.admin.amx.intf.config;


public interface ExternalJndiResource extends NamedConfigElement, PropertiesAccess, SystemPropertiesAccess {


    public String getEnabled();

    public void setEnabled(String param1);

    public String getDescription();

    public void setDescription(String param1);

    public String getResType();

    public void setResType(String param1);

    public String getFactoryClass();

    public void setFactoryClass(String param1);

    public String getJndiLookupName();

    public void setJndiLookupName(String param1);

}
