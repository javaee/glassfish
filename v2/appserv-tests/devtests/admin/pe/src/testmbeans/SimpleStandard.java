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
 * SimpleStandard.java
 *
 * Created on Sat Jul 02 01:54:54 PDT 2005
 */
package testmbeans;
import javax.management.*;

/**
 * Class SimpleStandard
 * SimpleStandard Description
 */
public class SimpleStandard implements SimpleStandardMBean, NotificationEmitter
{
    /** Attribute : Color */
    private String color = null;

    /** Attribute : State */
    private boolean state = false;

   /* Creates a new instance of SimpleStandard */
    public SimpleStandard()
    {
    }

   /**
    * Get This is the Color Attribute.
    */
    public String getColor()
    {
        return color;
    }

   /**
    * Set This is the Color Attribute.
    */
    public void setColor(String value)
    {
        color = value;
    }

   /**
    * Get This is the State Attribute
    */
    public boolean getState()
    {
        return state;
    }

   /**
    * Set This is the State Attribute
    */
    public void setState(boolean value)
    {
        state = value;
    }

   /**
    * Greets someone
    * @param name <code>String</code> The person to greet
    */
    public void greet(String name)
    {
        System.out.println("Hello, " + name);
    }

   /**
    * MBean Notification support
    * You shouldn't update these methods
    */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    public void addNotificationListener(NotificationListener listener,
       NotificationFilter filter, Object handback)
       throws IllegalArgumentException {
         broadcaster.addNotificationListener(listener, filter, handback);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
         return new MBeanNotificationInfo[] {
               new MBeanNotificationInfo(new String[] {
                      AttributeChangeNotification.ATTRIBUTE_CHANGE},
                      javax.management.AttributeChangeNotification.class.getName(),
                      "Usual Attribute Change Notification")
                };
    }

    public void removeNotificationListener(NotificationListener listener)
       throws ListenerNotFoundException {
         broadcaster.removeNotificationListener(listener);
    }

    public void removeNotificationListener(NotificationListener listener,
       NotificationFilter filter, Object handback)
       throws ListenerNotFoundException {
         broadcaster.removeNotificationListener(listener, filter, handback);
    }
    // </editor-fold> 

    private synchronized long getNextSeqNumber() {
         return seqNumber++;
    }

    private long seqNumber;
    private final NotificationBroadcasterSupport broadcaster =
               new NotificationBroadcasterSupport();
   
}
