/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/**
 * ImplementsMBeanRegistration.java
 *
 * Created on March 17, 2006, 5:59 PM
 */
package testmbeans;
import javax.management.*;
import java.lang.reflect.Constructor;

/**
 * ImplementsMBeanRegistration Dynamic MBean
 * ImplementsMBeanRegistration Description
 * @author kedarm
 */
public class ImplementsMBeanRegistration extends ImplementsMBeanRegistrationDynamicSupport implements MBeanRegistration
{
    /* Creates a new instance of ImplementsMBeanRegistration */
    public ImplementsMBeanRegistration()
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
        myName = name;
        return name;
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
