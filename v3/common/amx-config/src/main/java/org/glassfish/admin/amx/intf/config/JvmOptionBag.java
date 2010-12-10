package org.glassfish.admin.amx.intf.config;

import java.util.List;

public interface JvmOptionBag {


    public boolean contains(String param1);

    public List getJvmOptions();

    public void setJvmOptions(List param1);

    public int getXmxMegs();

    public int getXmsMegs();

    public String getStartingWith(String param1);

}
