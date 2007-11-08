/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
/**
 * ObjectNameSelfProvider.java
 *
 * Created on Sat Jul 02 02:07:27 PDT 2005
 */
package testmbeans;
import javax.management.*;
import java.util.*;
import java.lang.reflect.Constructor;

/**
 * ObjectNameSelfProvider Dynamic MBean
 * ObjectNameSelfProvider Description
 * @author kedarm
 */
public class ObjectNameSelfProvider extends ObjectNameSelfProviderDynamicSupport implements MBeanRegistration
{
    private final ObjectName myON;
   /* Creates a new instance of ObjectNameSelfProvider */
    public ObjectNameSelfProvider() throws Exception
    {
        buildDynamicMBeanInfo();
        myON = new ObjectName("user:type=myself");
    }

   /**
    * Gets the value of the specified attribute of the DynamicMBean.
    * @param attributeName The attribute name
    */
   public Object getAttribute(String attributeName)
       throws AttributeNotFoundException,
              MBeanException,
              ReflectionException {

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
              ReflectionException {

        throw new AttributeNotFoundException("Unknown Attribute "
                      + attribute.getName());
   }

   /**
    * Allows an operation to be invoked on the DynamicMBean.
    */
   public Object invoke(String operationName,
                        Object params[],
                        String signature[])
       throws MBeanException, ReflectionException {
       throw new MBeanException(
              new IllegalArgumentException("UnKnown Operation " +
                                           operationName));
   }

   /**
    * Create the MBeanInfoConstructors.
    * WARNING : if you add constructors to ObjectNameSelfProvider class,
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
   private void buildDynamicMBeanInfo() {
       MBeanAttributeInfo[] dAttributes = new MBeanAttributeInfo[] {
       };
       MBeanConstructorInfo[] dConstructors = createConstructors();
       MBeanOperationInfo[] dOperations = new MBeanOperationInfo[] {
       };
       dNotifications = new MBeanNotificationInfo[] {
        };
       dMBeanInfo = new MBeanInfo("testmbeans.ObjectNameSelfProvider",
                                  "ObjectNameSelfProvider Description",
                                  dAttributes,
                                  dConstructors,
                                  dOperations,
                                  dNotifications);
   }
   // </editor-fold>


    private MBeanNotificationInfo[] dNotifications;
    private MBeanInfo dMBeanInfo;

    public void postDeregister() {
        System.out.println("Post Registration on: " + this.getClass().getName());
    }

    public void postRegister(Boolean booleanParam) {
    }

    public void preDeregister() throws Exception {
    }

    public ObjectName preRegister(MBeanServer mBeanServer, ObjectName objectName) throws Exception {
        return (myON);
    }
}
