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
public class MBeanListener<T extends MBeanListener.Callback> implements NotificationListener
{
    private static void debug(final Object o) { System.out.println( "" + o ); }
    
    private final String mType;
    private final String mName;
    
    /** mType and mName should be null if mObjectName is non-null, and vice versa */
    private final ObjectName mObjectName;

    private final MBeanServer mMBeanServer;

    private final T mCallback;

    public String toString()
    {
        return "MBeanListener: ObjectName=" + mObjectName + ", type=" + mType + ", name=" + mName;
    }
    
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
    
    /** Callback interface.  */
    public interface Callback
    {
        public void mbeanRegistered(final ObjectName objectName, final MBeanListener listener);
        public void mbeanUnregistered(final ObjectName objectName, final MBeanListener listener);
    }
    
    /**
        Default callback implementation, can be subclassed if needed
        Remembers only the last MBean that was seen.
     */
    public static class CallbackImpl implements MBeanListener.Callback
    {
        private volatile ObjectName mRegistered = null;
        private volatile ObjectName mUnregistered = null;
        private final boolean mStopAtFirst;
        
        public CallbackImpl() {
            this(true);
        }
        
        public CallbackImpl(final boolean stopAtFirst)
        {
            mStopAtFirst = stopAtFirst;
        }
        
        public ObjectName getRegistered()   { return mRegistered; }
        public ObjectName getUnregistered() { return mUnregistered; }
        
        public void mbeanRegistered(final ObjectName objectName, final MBeanListener listener)
        {
            mRegistered = objectName;
            if ( mStopAtFirst )
            {
                listener.stopListening();
            }
        }
        public void mbeanUnregistered(final ObjectName objectName, final MBeanListener listener)
        {
            mUnregistered = objectName;
            if ( mStopAtFirst )
            {
                listener.stopListening();
            }
        }
    }
    
    public T getCallback()
    {
        return mCallback;
    }
 
    /**
     * Listener for a specific MBean.
     * Caller must call {@link #start} to start listening.
     * @param server
     * @param objectName
     * @param callback
     */
    public MBeanListener(
            final MBeanServer server,
            final ObjectName objectName,
            final T callback)
    {
        mMBeanServer = server;
        mObjectName = objectName;
        mType = null;
        mName = null;
        mCallback = callback;
    }
    
    /**
     * Listener for all MBeans of specified type, with or without a name.
     * Caller must call {@link #start} to start listening.
     * @param server
     * @param type type of the MBean (as found in the ObjectName)
     * @param callback
     */
    public MBeanListener(
            final MBeanServer server,
            final String type,
            final T callback)
    {
        this(server, type, null, callback);
    }

    /**
     * Listener for MBeans of specified type, with specified name (or any name
     * if null is passed for the name).
     * Caller must call {@link #start} to start listening.
     * @param server
     * @param type type of the MBean (as found in the ObjectName)
     * @param name name of the MBean, or null if none
     * @param callback
     */
    public MBeanListener(
            final MBeanServer server,
            final String type,
            final String name,
            final T callback)
    {
        mMBeanServer = server;
        mType = type;
        mName = name;
        mObjectName = null;
        mCallback = callback;
    }

    /**
        Listen for the registration of AMX DomainRoot 
        Listening starts automatically.
     */
    public static <T extends Callback> MBeanListener<T> listenForDomainRoot(
        final MBeanServer server,
        final T callback)
    {
        final MBeanListener<T> listener = new MBeanListener<T>( server, AMXValues.domainRoot(), callback);
        listener.start();
        return listener;
    }
    
    /**
        Listen for the registration of the {@link BootAMXMBean}.
        Listening starts automatically.
     */
    public static <T extends Callback> MBeanListener<T> listenForBootAMX(
        final MBeanServer server,
        final T callback)
    {
        final MBeanListener<T> listener = new MBeanListener<T>( server, BootAMXMBean.OBJECT_NAME, callback);
        listener.start();
        return listener;
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

        if ( mObjectName != null )
        {
            if ( mMBeanServer.isRegistered(mObjectName) )
            {
                mCallback.mbeanRegistered(mObjectName, this);
            }
        }
        else
        {
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
    }

    /** unregister the listener */
    public void stopListening()
    {
        try
        {
            mMBeanServer.removeNotificationListener(AMXValues.getMBeanServerDelegateObjectName(), this);
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Can't remove NotificationListener " + this, e);
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

            boolean match = false;
            if ( mObjectName != null && mObjectName.equals(objectName) )
            {
                match = true;
            }
            else if ( objectName.getDomain().equals( AMXValues.amxJMXDomain() ) )
            {
                if ( mType != null && mType.equals(objectName.getKeyProperty(TYPE_KEY)) )
                {
                    final String mbeanName = objectName.getKeyProperty(NAME_KEY);
                    if (mName != null && mName.equals(mbeanName))
                    {
                        match = true;
                    }
                }
            }
            
            if ( match )
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



