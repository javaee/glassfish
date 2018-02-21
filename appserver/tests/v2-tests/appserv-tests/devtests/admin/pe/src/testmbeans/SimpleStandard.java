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
