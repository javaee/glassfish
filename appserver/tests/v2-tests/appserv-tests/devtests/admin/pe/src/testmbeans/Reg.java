/**
 * ImplementsMBeanRegistration.java
 *
 * Created on March 17, 2006, 5:59 PM
 */
package testmbeans;
import javax.management.*;
import java.lang.reflect.Constructor;

public class Reg extends ImplementsMBeanRegistrationDynamicSupport implements MBeanRegistration
{
    /* Creates a new instance of ImplementsMBeanRegistration */
    public Reg()
{
        buildDynamicMBeanInfo();
    }
    
    /**
     * Gets the value of the specified attribute of the DynamicMBean.
     * @param attributeName The attribute name
     */
    public Object getAttribute(String attributeName)
    throws AttributeNotFoundException,
            MBeanException,
            ReflectionException  {

        if (attributeName.equals("Name")) {
            
            //TODO return value of Name attribute
            
            return null;
        }
        
        throw new AttributeNotFoundException("Unknown Attribute "
            + attributeName);
    }
    
    /**
     * Sets the value of the specified attribute of the DynamicMBean.
     * @param attribute The attribute to set
     */
    public void setAttribute(Attribute attribute)
    throws AttributeNotFoundException,
            InvalidAttributeValueException,
            MBeanException,
            ReflectionException  {

        if (attribute.getName().equals("Name")) {
            
            //TODO set value of Name attribute
            
        } else 
            throw new AttributeNotFoundException("Unknown Attribute "
            + attribute.getName());
    }
    
    /**
     * Allows an operation to be invoked on the DynamicMBean.
     */
    public Object invoke(String operationName,
            Object params[],
            String signature[])
            throws MBeanException, ReflectionException  {

        throw new MBeanException(
            new IllegalArgumentException("Unknown Operation " +
            operationName));
    }
    
    /**
     * Create the MBeanInfoConstructors.
     * WARNING : if you add constructors to ImplementsMBeanRegistration class,
     * you will have to update this method.
     */
    // <editor-fold defaultstate="collapsed" desc=" MBeanInfo Support Code ">
    private MBeanConstructorInfo[] createConstructors() {
        return super.createConstructors(getClass());
    }
    
    /**
     * you shouldn't update the following code.
     */
    public MBeanInfo getMBeanInfo() {
        return dMBeanInfo;
    }
    
    /**
     * Build the private dMBeanInfo field,
     * which represents the management interface exposed by the MBean,
     * that is, the set of attributes, constructors, operations and
     * notifications which are available for management.
     *
     * A reference to the dMBeanInfo object is returned by the getMBeanInfo()
     * method of the DynamicMBean interface. Note that, once constructed,
     * an MBeanInfo object is immutable.
     */
    private void buildDynamicMBeanInfo()  {
        MBeanAttributeInfo[] dAttributes = new MBeanAttributeInfo[] {
            new MBeanAttributeInfo("Name",
                java.lang.String.class.getName(),
                "A Name",
                true,
                true,
                false)
            };
        MBeanConstructorInfo[] dConstructors = createConstructors();
        MBeanOperationInfo[] dOperations = new MBeanOperationInfo[] {
        };
        dMBeanInfo = new MBeanInfo("ImplementsMBeanRegistration",
            "ImplementsMBeanRegistration Description",
            dAttributes,
            dConstructors,
            dOperations,
            getNotificationInfo());
    }
    // </editor-fold>
    
    public  MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[] {};
    }
    
    private MBeanInfo dMBeanInfo;

    /**
     * Allows the MBean to perform any operations it needs before being
     * registered in the MBean server. If the name of the MBean is not
     * specified, the MBean can provide a name for its registration. If
     * any exception is raised, the MBean will not be registered in the
     * MBean server.
     * @param server The MBean server in which the MBean will be registered.
     * @name name The object name of the MBean. This name is null if the
     * name parameter to one of the createMBean or registerMBean methods in
     * the MBeanServer interface is null. In that case, this method must
     * return a non-null ObjectName for the new MBean.
     * @return The name under which the MBean is to be registered. This value
     * must not be null. If the name parameter is not null, it will usually
     * but not necessarily be the returned value.
     * @trow Exception This exception will be caught by the MBean server and
     * re-thrown as an MBeanRegistrationException.
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception {
        return new ObjectName("user", "foo", "xyz");
    }

    /**
     * Allows the MBean to perform any operations needed after having
     * been registered in the MBean server or after the registration has
     * failed.
     * @param registrationDone Indicates wether or not the MBean has been
     * successfully registered in the MBean server. The value false means
     * that the registration has failed.
     */
    public void postRegister(Boolean registrationDone) {
        //TODO postRegister implementation;
    }

    /**
     * Allows the MBean to perform any operations it needs before being
     * unregistered by the MBean server.
     * @trow Exception This exception will be caught by the MBean server and
     * re-thrown as an MBeanRegistrationException.
     */
    public void preDeregister() throws Exception {
        //TODO preDeregister implementation;
    }

    /**
     * Allows the MBean to perform any operations needed after having been
     * unregistered in the MBean server.
     */
    public void postDeregister() {
        //TODO postDeregister implementation;
    }
    
    
    private ObjectName myName;
}
