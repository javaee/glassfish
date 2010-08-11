package com.sun.enterprise.connectors.jms.system;

import com.sun.enterprise.config.serverbeans.JmsHost;
import java.beans.PropertyVetoException;
import org.jvnet.hk2.config.types.Property;
import java.util.List;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.TransactionFailure;
/** A wrapper class for the JmsHost serverbean since you cannot clone JmsHost
 * elements without being a part of a transaction
 * This class is primaritly used to create copies of the default jmshost element
 * during auto-clustering
*/

public class JmsHostWrapper implements JmsHost{
    private String name;
    public String getName(){
        return name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException
    {
        name = value;
    }

    /**
     * Gets the value of the host property.
     *
     * @return possible object is
     *         {@link String }
     */
    private String host;
    public String getHost(){
        return host;
    }

    /**
     * Sets the value of the host property.
     *
     * ip V6 or V4 address or hostname
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setHost(String value) throws PropertyVetoException{
        host = value;
    }

    /**
     * Gets the value of the port property.
     *
     * Port number used by the JMS service
     *
     * @return possible object is
     *         {@link String }
     */
    private String port = "7676";
    public String getPort()
    {
        return port;
    }

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPort(String value) throws PropertyVetoException{
        //only set the value if it is not null. Incase it is null, use the default value
        if (value != null)
            port = value;
    }

    /**
     * Gets the value of lazyInit property
     *
     * if false, this listener is started during server startup
     *
     * @return true or false
     */
    private String lazyInit;
    public String getLazyInit(){
        return lazyInit;
    }

    /**
     * Sets the value of lazyInit property
     *
     * Specify is this listener should be started as part of server startup or not
     *
     * @param value true if the listener is to be started lazily; false otherwise
     */
    public void setLazyInit(String value){
        lazyInit = value;
    }

    /**
     * Gets the value of the adminUserName property.
     *
     * Specifies the admin username
     *
     * @return possible object is
     *         {@link String }
     */

    private String adminUserName;
    public String getAdminUserName(){
        return adminUserName;
    }

    /**
     * Sets the value of the adminUserName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setAdminUserName(String value) throws PropertyVetoException
    {
        adminUserName = value;
    }

    /**
     * Gets the value of the adminPassword property.
     *
     * Attribute specifies the admin password
     *
     * @return possible object is
     *         {@link String }
     */
     private String adminPassword;
    public String getAdminPassword(){
           return  adminPassword;
    }

    public void setAdminPassword(String value) throws PropertyVetoException
    {
        adminPassword = value;
    }

    private List<Property> properties = null;
    public List<Property> getProperty()
    {
          return properties;
    }
    public ConfigBeanProxy deepCopy(ConfigBeanProxy proxy)
    {

        JmsHostWrapper clone = new JmsHostWrapper();
        if(! (proxy instanceof JmsHost)) return null;

        JmsHost host = (JmsHost)proxy;
        try{
        clone.setHost(host.getHost());
        clone.setPort(host.getPort());
        clone.setName(host.getName());
        clone.setAdminPassword(host.getAdminPassword());
        clone.setAdminUserName(host.getAdminUserName());
        }catch (PropertyVetoException e)
        {
            //todo : handle this exception
        }

        return null;
    }
    /* The following methods are dummy methods required as part of the interface implentation.*/

    public <T extends ConfigBeanProxy> T createChild(Class<T> type) throws TransactionFailure
    {
        return null;
    }
    public ConfigBeanProxy getParent()
    {
        return null;
    }
    public <T extends ConfigBeanProxy> T getParent(Class<T> type){
        return null;
    }
    public void injectedInto(Object target)    {
        //do nothing
    }
    public String getPropertyValue (String str, String str2){
        return null;
    }
    public String getPropertyValue (String str){
        return null;
    }
     public Property getProperty (String str){
        return null;
    }
}
