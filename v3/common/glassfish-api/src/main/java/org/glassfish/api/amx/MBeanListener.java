/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.api.amx;

import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import static org.glassfish.api.amx.AMXValues.*;

/**
 * Listens for registration of MBeans of various types.
 * Intended usage is for subsystems to lazy-load only when the Parent
 * MBean is registered.
 */
public class MBeanListener implements NotificationListener
{
    private final String mType;

    private final String mName;

    private final MBeanServer mMBeanServer;

    private final Callback mCallback;

    public String getType()
    {
        return mType;
    }

    public String getName()
    {
        return mName;
    }

    public MBeanServer getMBeanServer()
    {
        return mMBeanServer;
    }

    public Callback getCallback()
    {
        return mCallback;
    }

    public interface Callback
    {
        /**
        Subclass should override this method, verifying the ObjectName represents
        the desired MBean.
         */
        public void mbeanRegistered(final ObjectName objectName, final MBeanListener listener);

        /**
        Subclass should override this method, verifying the ObjectName represents
        the desired MBean.
         */
        public void mbeanUnregistered(final ObjectName objectName, final MBeanListener listener);

    }

    /**
     * Listener for all MBeans of specified type, with or without a name.
     * @param type type of the MBean (as found in the ObjectName)
     * @param handback arbitrary value passed to {@link mbeanRegistered}
     */
    public MBeanListener(
            final MBeanServer server,
            final String type,
            final Callback callback)
    {
        this(server, type, null, callback);
    }

    /**
     * Listener for MBeans of specified type, with specified name (or any name
     * if null is passed for the name).
     * @param type type of the MBean (as found in the ObjectName)
     * @param name name of the MBean, or null if none
     * @param handback arbitrary value passed to {@link mbeanRegistered}
     */
    public MBeanListener(
            final MBeanServer server,
            final String type,
            final String name,
            final Callback callback)
    {
        mMBeanServer = server;
        mType = type;
        mName = name;
        mCallback = callback;
    }

    public static MBeanListener listenForDomainRoot(
        final MBeanServer server,
        final Callback callback)
    {
        final String type = AMXValues.domainRoot().getKeyProperty(TYPE_KEY);
        return new MBeanListener( server, type, callback);
    }

    /**
    Start listening.  If the required MBean(s) are already present, the callback
    will be synchronously made before returning.  It is also possible that the
    callback could happen twice for the same MBean.
     */
    public void start()
    {
        // race condition: must listen *before* looking for existing MBeans
        try
        {
            mMBeanServer.addNotificationListener(AMXValues.getMBeanServerDelegateObjectName(), this, null, this);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Can't add NotificationListener", e);
        }

        // query for AMX MBeans of the requisite type
        String props = TYPE_KEY + "=" + mType;
        if (mName != null)
        {
            props = props + "," + NAME_KEY + mName;
        }

        final ObjectName pattern = AMXValues.newObjectName(AMXValues.amxJMXDomain(), props);
        final Set<ObjectName> matched = mMBeanServer.queryNames(pattern, null);
        for (final ObjectName objectName : matched)
        {
            mCallback.mbeanRegistered(objectName, this);
        }
    }

    /** unregister the listener */
    public void stop()
    {
        try
        {
            mMBeanServer.removeNotificationListener(AMXValues.getMBeanServerDelegateObjectName(), this);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Can't remove NotificationListener", e);
        }
    }

    public void handleNotification(
            final Notification notifIn,
            final Object handback)
    {
        if (notifIn instanceof MBeanServerNotification)
        {
            final MBeanServerNotification notif = (MBeanServerNotification) notifIn;
            final ObjectName objectName = notif.getMBeanName();
            final String mbeanType = objectName.getKeyProperty(TYPE_KEY);
            final String mbeanName = objectName.getKeyProperty(NAME_KEY);

            if (mType.equals(mbeanType))
            {
                if (mName != null && mName.equals(mbeanName))
                {
                    final String notifType = notif.getType();
                    if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notifType))
                    {
                        mCallback.mbeanRegistered(objectName, this);
                    }
                    else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(notifType))
                    {
                        mCallback.mbeanUnregistered(objectName, this);
                    }
                }
            }
        }
    }

}



