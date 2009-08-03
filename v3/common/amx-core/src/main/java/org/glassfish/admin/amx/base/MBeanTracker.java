
package org.glassfish.admin.amx.base;


import java.util.Collections;
import javax.management.*;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.util.jmx.JMXUtil;

import org.glassfish.api.amx.AMXMBeanMetadata;
import org.glassfish.external.amx.AMX;
import org.glassfish.external.amx.AMXGlassfish;

/**
    Tracks the entire MBean parent/child hierarachy so that individual MBeans need not do so.
    Can supply parents and children of any MBean, used by all AMX implementations.
 */
@Taxonomy(stability = Stability.NOT_AN_INTERFACE)
@AMXMBeanMetadata(singleton=true, globalSingleton=true, leaf=true)
public final class MBeanTracker implements NotificationListener, MBeanRegistration, MBeanTrackerMBean
{
    private static void debug(final Object o)
    {
        System.out.println( "" + o);
    }
    
    /** maps a parent ObjectName to a Set of children */
    final ConcurrentMap<ObjectName,Set<ObjectName>> mParentChildren;
    
    /** maps a child to its parent, needed because when unregistered we can't obtain parent */
    final ConcurrentMap<ObjectName,ObjectName>      mChildParent;
    
    private volatile MBeanServer mServer;
    private volatile ObjectName  mObjectName;
    
    public MBeanTracker()
    {
        mParentChildren = new ConcurrentHashMap<ObjectName,Set<ObjectName>>();
        mChildParent    = new ConcurrentHashMap<ObjectName,ObjectName>();
    }
    
    public void handleNotification(final Notification notifIn, final Object handback)
    {
        if ( notifIn instanceof MBeanServerNotification )
        {
            final MBeanServerNotification notif = (MBeanServerNotification)notifIn;
            
            final String type = notif.getType();
            final ObjectName objectName = notif.getMBeanName();
            
            // what happens if an MBean is removed before we can add it
            // eg the MBeanServer uses more than one thread to deliver notifications
            // to use? Even if we synchronize this method, the remove could still arrive
            // first and there's nothing we could do about it.
            if ( type.equals( MBeanServerNotification.REGISTRATION_NOTIFICATION ) )
            {
                addChild(objectName);
            }
            else if ( type.equals( MBeanServerNotification.UNREGISTRATION_NOTIFICATION ) )
            {
                //debug( "MBeanTracker.handleNotification: MBean unregistered: " + objectName );
                removeChild(objectName);
            }
        }
    }
    
	public ObjectName preRegister(
		final MBeanServer	server,
		final ObjectName	nameIn)
		throws Exception
	{
		mServer			= server;
        mObjectName     = nameIn;
		return( nameIn );
	}
    
		public final void
	postRegister( final Boolean registrationSucceeded )
	{	
		if ( registrationSucceeded.booleanValue() )
		{
            try
            {
                mServer.addNotificationListener( JMXUtil.getMBeanServerDelegateObjectName(), this, null, null );
            }
            catch( Exception e )
            {
                e.printStackTrace();
                throw new RuntimeException("Could not register with MBeanServerDelegate", e);
            }
            //debug( "MBeanTracker: registered as " + mObjectName );
		}
        // populate our list
		final ObjectName	pattern	= Util.newObjectNamePattern( AMXGlassfish.DEFAULT.amxJMXDomain(), "" );
		final Set<ObjectName>	names	= JMXUtil.queryNames( mServer, pattern, null );
        //debug( "MBeanTracker: found MBeans: " + names.size() );
        for( final ObjectName o : names )
        {
            addChild(o);
        }
	}
    
	public final void preDeregister() throws Exception
    {
        mServer.removeNotificationListener( mObjectName, this);
    }
    
	public final void postDeregister() {
    }
    
    private void addChild(final ObjectName child)
    {
        ObjectName parent = null;
        try {
            parent = (ObjectName)mServer.getAttribute(child, AMX.ATTR_PARENT);
        }
        catch( final Exception e )
        {
            // nothing to be done, MBean gone missing, badly implemented, etc.
        }
        
        if ( parent != null )
        {
            synchronized(this)
            {
                mChildParent.put(child, parent);
                Set<ObjectName> children = mParentChildren.get(parent);
                if ( children == null )
                {
                    children = new HashSet<ObjectName>();
                    mParentChildren.put(parent, children);
                }
                children.add(child);
                //debug( "MBeanTracker: ADDED " + child + " with parent " + parent );
            }
        }
    }
    
    /**
        Must be 'synchronized' because we're working on two different Maps.
     */
    private synchronized ObjectName removeChild(final ObjectName child)
    {
        final ObjectName parent = mChildParent.remove(child);
        if ( parent != null )
        {
            final Set<ObjectName> children = mParentChildren.get(parent);
            if ( children != null )
            {
                children.remove(child);
                if ( children.size() == 0 )
                {
                    mParentChildren.remove(parent);
                    //debug( "MBeanTracker: REMOVED " + child + " from parent " + parent );
                }
            }
        }
        return parent;
    }
    
    public ObjectName getParentOf(final ObjectName child)
    {
        return mChildParent.get(child);
    }
    
        public synchronized Set<ObjectName>
    getChildrenOf(final ObjectName parent)
    {
        final Set<ObjectName> children = mParentChildren.get(parent) ;
        if ( children == null )
        {
            return Collections.emptySet();
        }
        
        return new HashSet<ObjectName>(children);
    }
}


















