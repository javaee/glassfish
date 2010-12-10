package org.glassfish.admin.amx.intf.config;


public interface FailureDetection extends NamedConfigElement, PropertiesAccess, SystemPropertiesAccess {


    public String getMaxMissedHeartbeats();

    public void setMaxMissedHeartbeats(String param1);

    public String getHeartbeatFrequencyInMillis();

    public void setHeartbeatFrequencyInMillis(String param1);

    public void setVerifyFailureWaittimeInMillis(String param1);

    public String getVerifyFailureWaittimeInMillis();

    public void setVerifyFailureConnectTimeoutInMillis(String param1);

    public String getVerifyFailureConnectTimeoutInMillis();

}
