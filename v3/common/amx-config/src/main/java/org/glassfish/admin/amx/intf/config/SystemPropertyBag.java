package org.glassfish.admin.amx.intf.config;


public interface SystemPropertyBag extends AnyProperty {


    public String getPropertyValue(String param1, String param2, String param3);

    public boolean containsProperty(String param1);

}
