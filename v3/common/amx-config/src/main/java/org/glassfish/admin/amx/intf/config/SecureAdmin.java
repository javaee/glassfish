package org.glassfish.admin.amx.intf.config;


public interface SecureAdmin {


    public String getEnabled();

    public void setEnabled(String param1);

    public String getSpecialAdminIndicator();

    public void setSpecialAdminIndicator(String param1);

    public String dasAlias();

    public void setDasAlias(String param1);

    public String instanceAlias();

    public void setInstanceAlias(String param1);

    public String getInstanceAlias();

    public String getDasAlias();

    public boolean isEnabled();

}
