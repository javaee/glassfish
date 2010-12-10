package org.glassfish.admin.amx.intf.config;

import java.util.Map;

public interface RefContainer {


    public Map<String, ResourceRef> getResourceRef();

    public Map<String, ApplicationRef> getApplicationRef();

}
