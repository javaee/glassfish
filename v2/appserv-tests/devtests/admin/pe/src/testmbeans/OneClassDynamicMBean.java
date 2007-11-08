/**
 * OneClassDynamicMBean.java
 *
 * Created on Mon Aug 08 00:27:27 PDT 2005
 */
package testmbeans;
import java.lang.reflect.Constructor;
import javax.management.*;
import java.util.*;


/**
 * OneClassDynamicMBean Dynamic MBean
 * OneClassDynamicMBean Description
 * @author kedarm
 */
public class OneClassDynamicMBean implements DynamicMBean
{
   /* Creates a new instance of OneClassDynamicMBean */
    public OneClassDynamicMBean()
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
              ReflectionException {

        if (attributeName.equals("A1")) {

           //TODO return value of A1 attribute

           return null;
        }

        if (attributeName.equals("A2")) {

           //TODO return value of A2 attribute

           return null;
        }

        if (attributeName.equals("A3")) {

           //TODO return value of A3 attribute

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
              ReflectionException {

        if (attribute.getName().equals("A1")) {

           //TODO set value of A1 attribute

        } else if (attribute.getName().equals("A2")) {

           //TODO set value of A2 attribute

        } else if (attribute.getName().equals("A3")) {

           //TODO set value of A3 attribute

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
       throws MBeanException, ReflectionException {
       String[] methodSignature;

       methodSignature = new String[] {
       };
       if (operationName.equals("sayHi") && Arrays.equals(signature, methodSignature)) {
           
           //TODO add your code here
           
           return null;
       }

       throw new MBeanException(
              new IllegalArgumentException("Unknown Operation " +
                                           operationName));
   }

   /**
    * Create the MBeanInfoConstructors.
    * WARNING : if you add constructors to OneClassDynamicMBean class,
    * you will have to update this method.
    */
   // <editor-fold defaultstate="collapsed" desc=" MBeanInfo Support Code ">
   private MBeanConstructorInfo[] createConstructors() {
       final Class clzz = this.getClass();
        Constructor[] constructors = clzz.getConstructors();
        MBeanConstructorInfo[] infos = new MBeanConstructorInfo[
              constructors.length];
        for (int i = 0; i < constructors.length; i++) {
             infos[i] = new MBeanConstructorInfo("Constructs a"
                   + clzz.getName() + "object", constructors[i]);
        }
        return infos;
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
           new MBeanAttributeInfo("A1",
                                  java.lang.String.class.getName(),
                                  "A1",
                                  true,
                                  true,
                                  false),
           new MBeanAttributeInfo("A2",
                                  java.lang.Boolean.TYPE.getName(),
                                  "A2",
                                  true,
                                  true,
                                  true),
           new MBeanAttributeInfo("A3",
                                  javax.management.ObjectName.class.getName(),
                                  "ObjectName A3",
                                  true,
                                  true,
                                  false)
       };
       MBeanConstructorInfo[] dConstructors = createConstructors();
       MBeanParameterInfo[] sayHiParams = new MBeanParameterInfo[] {
       };
       MBeanOperationInfo[] dOperations = new MBeanOperationInfo[] {
           new MBeanOperationInfo("sayHi",
                                  "Greet",
                                  sayHiParams,
                                  java.lang.Void.TYPE.getName(),
                                  MBeanOperationInfo.ACTION)
       };
       dNotifications = new MBeanNotificationInfo[] {
        };
       dMBeanInfo = new MBeanInfo("OneClassDynamicMBean",
                                  "OneClassDynamicMBean Description",
                                  dAttributes,
                                  dConstructors,
                                  dOperations,
                                  dNotifications);
   }
   // </editor-fold>


    private MBeanNotificationInfo[] dNotifications;
    private MBeanInfo dMBeanInfo;

    public AttributeList getAttributes(String[] str) {
        return ( new AttributeList() );
    }

    public AttributeList setAttributes(AttributeList attributeList) {
        return ( new AttributeList() );
    }
}
