package org.glassfish.admin.amx.intf.config;


public interface HttpProtocol {


    public String getVersion();

    public String getDefaultResponseType();

    public void setDefaultResponseType(String param1);

    public String getForcedResponseType();

    public void setForcedResponseType(String param1);

    public void setVersion(String param1);

    public String getDnsLookupEnabled();

    public void setDnsLookupEnabled(String param1);

    public String getForcedType();

    public void setForcedType(String param1);

    public String getDefaultType();

    public void setDefaultType(String param1);

    public String getSslEnabled();

    public void setSslEnabled(String param1);

}
