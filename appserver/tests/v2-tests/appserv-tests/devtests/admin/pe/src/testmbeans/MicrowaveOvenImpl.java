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
 * MicrowaveOven.java
 *
 * Created on Sat Jul 02 02:03:02 PDT 2005
 */
package testmbeans;
import javax.management.*;

/**
 * Dynamic MBean based on StandardMBean
 * Class MicrowaveOvenImpl
 * MicrowaveOvenImpl Description
 * Note that this is an "extended" Standard MBean in that it does not need to follow the standard MBean design patterns.
 * This MBean implements a Standard MBean interface called MicrowaveOven.
 */
public class MicrowaveOvenImpl extends javax.management.StandardMBean
    implements MicrowaveOven, NotificationEmitter {

    /** Attribute : Color */
    private String color = null;

    /** Attribute : NoFunctions */
    private int noFunctions = 0;

    /** Attribute : Make */
    private String make = null;

    /** Attribute : Timer */
    private int timer = 0;

    /** Attribute : State */
    private boolean state = false;

   /* Creates a new instance of MicrowaveOvenImpl */
    public MicrowaveOvenImpl() throws NotCompliantMBeanException {
         super(MicrowaveOven.class);
         color = "BLACK";
         noFunctions = 5;
         make = "GE";
    }

   /**
    * Get Color of the oven
    */
    public String getColor()
    {
        return color;
    }

   /**
    * Get Number of functions in the oven
    */
    public int getNoFunctions()
    {
        return noFunctions;
    }

   /**
    * Get Make of the oven
    */
    public String getMake()
    {
        return make;
    }

   /**
    * Get How long it should be heated in seconds
    */
    public int getTimer()
    {
        return timer;
    }

   /**
    * Set How long it should be heated in seconds
    */
    public void setTimer(int value)
    {
        timer = value;
    }

   /**
    * Get NewAttribute4 Description
    */
    public boolean getState()
    {
        return state;
    }

   /**
    * Starts the Oven
    */
    public void start()
    {
        //TODO Add the operation implementation
    }

   /**
    * Stops the Oven
    */
    public void stop()
    {
        //TODO Add the operation implementation
    }

   /*
    * Next are the methods to compute MBeanInfo.
    * You shouldn't update these methods
    */
    protected String getDescription(MBeanInfo info) {
         return "MicrowaveOven Description";
    }

    protected String getDescription(MBeanAttributeInfo info) {
        String description = null;
        if (info.getName().equals("Color")) {
             description = "Color of the oven";
        } else if (info.getName().equals("NoFunctions")) {
             description = "Number of functions in the oven";
        } else if (info.getName().equals("Make")) {
             description = "Make of the oven";
        } else if (info.getName().equals("Timer")) {
             description = "How long it should be heated, in seconds";
        } else if (info.getName().equals("State")) {
             description = "The state of microwave oven";
        }
        return description;
    }

    protected String getDescription(MBeanOperationInfo op,
                                    MBeanParameterInfo param,
                                    int sequence) {
        if (op.getName().equals("start")) {
           switch (sequence) {
             default : return null;
           }
        } else if (op.getName().equals("stop")) {
           switch (sequence) {
             default : return null;
           }
        }
        return null;
    }

    protected String getParameterName(MBeanOperationInfo op,
                                      MBeanParameterInfo param,
                                      int sequence) {
        if (op.getName().equals("start")) {
           switch (sequence) {
             default : return null;
           }
        } else if (op.getName().equals("stop")) {
           switch (sequence) {
             default : return null;
           }
        }
        return null;
    }

    protected String getDescription(MBeanOperationInfo info) {
        String description = null;
        if (info.getName().equals("start")) {
             description = "Starts the Oven";
        } else if (info.getName().equals("stop")) {
             description = "Stops the Oven";
        }
        return description;
    }

    public MBeanInfo getMBeanInfo() {
            MBeanInfo mbinfo = super.getMBeanInfo();
            return new MBeanInfo(mbinfo.getClassName(),
                                 mbinfo.getDescription(),
                                 mbinfo.getAttributes(),
                                 mbinfo.getConstructors(),
                                 mbinfo.getOperations(),
                                 getNotificationInfo());
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
                      "State changes")
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
