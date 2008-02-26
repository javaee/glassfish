
package org.glassfish.admin.amx.loader;

import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;
import javax.management.ObjectInstance;

import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;

import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.util.misc.RunnableBase;
import com.sun.appserv.management.util.misc.ClassUtil;
import com.sun.appserv.management.annotation.AMXConfigInfo;
import com.sun.appserv.management.annotation.AMXMBeanMetadata;


import org.glassfish.admin.amx.mbean.Delegate;
import org.glassfish.admin.amx.mbean.DelegateToConfigBeanDelegate;
import org.glassfish.admin.amx.mbean.AMXConfigImplBase;

import org.glassfish.admin.amx.util.ObjectNames;

/**
 * @author llc
 */
public final class AMXConfigLoader
{
    private static void debug( final String s ) { System.out.println(s); }
    
    private volatile MBeanServer mMBeanServer;
    
    private final LinkedBlockingQueue<ConfigBean> mPendingConfigBeans = new LinkedBlockingQueue<ConfigBean>();
    
    private AMXConfigLoaderThread mLoaderThread = null;
    
        public
    AMXConfigLoader()
    {
    }
    
    /**
        No items will be processd until {@link #start} is called.
     */
        protected void
    handleConfigBean( final ConfigBean cb )
    {
        /*
        Class<? extends ConfigBeanProxy> parentClass = null;
        final Class<? extends ConfigBeanProxy> cbClass = cb.getProxyType();
        final ConfigBean parent = asConfigBean( cb.parent() );
        if ( parent != null )
        {
            parentClass = parent.getProxyType();
        }
        //debug( "RECEIVED: " + cbClass.getName()  +
        //    ", PARENT = " + ((parentClass == null) ? "null" : parentClass.getName()) +
        //    ", parent Object Name = " + parent.getObjectName() );
        */
        
        mPendingConfigBeans.add( cb );
    }


     /**
        Internal nodes that don't get registered as MBeans throw a small monkey wrench
        into things; examples include &lt;configs> and &lt;resources> which are elements
        that contain only children, no Attributes of their own, and thus are not represented
        as MBeans.
     */
        private static ConfigBean
    getActualParent( final ConfigBean configBean )
    {
        ConfigBean parent = asConfigBean( configBean.parent() );
        if ( parent != null )
        {
            //debug( "config bean  " + configBean.getProxyType().getName() + " has parent " + configBean.parent().getProxyType().getName() );
            
            // if it has an ObjectName, then its a valid parent (this is the quick check).
            // If not, it might not yet have one; use the annotation
            final ObjectName parentObjectName    = parent.getObjectName();
            if ( parentObjectName == null )
            {
                // if parent is an internal node only, try its parent
                final Class<? extends ConfigBeanProxy> parentClass = parent.getProxyType();
                final AMXConfigInfo amxConfigInfo = parentClass.getAnnotation( AMXConfigInfo.class );
                if ( amxConfigInfo == null )
                {
                    throw new IllegalArgumentException( "ConfigBean has no @AMXConfigInfo: " + parentClass.getName() );
                }
                
                //debug( "amxInterface() for " + parent.getProxyType().getName() + " = " + amxConfigInfo.amxInterface().getName() );
                if ( amxConfigInfo.amxInterface() == AMXConfigVoid.class )
                {
                    parent = getActualParent( parent );
                }
            }
            else
            {
                // valid parent with an ObjectName
            }
        }
        else
        {
            if ( ! configBean.getProxyType().getName().endsWith( "Domain" ) )
            {
                debug( "WARNING: parent is null for " + configBean.getProxyType().getName() + " (bug in ConfigBeans)");
            }
        }
        
        return parent;
    }
    
        private static ObjectName
    getActualParentObjectName( final ConfigBean configBean )
    {
        ObjectName  parentObjectName = null;
        
        final ConfigBean parent = getActualParent( configBean );
        if ( parent != null )
        {
            parentObjectName    = parent.getObjectName();
        }
        
        return parentObjectName;
    }    
     /**
        @return a ConfigBean, or null if it's not a ConfigBean
     */
    @SuppressWarnings("unchecked")
    static ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
    }


    /**
        Enable registration of MBeans, queued until now.
     */
        public synchronized void
    start( final MBeanServer server )
    {
        if ( mLoaderThread == null )
        {
            mMBeanServer    = server;
            
            mLoaderThread   = new AMXConfigLoaderThread( mPendingConfigBeans );
            mLoaderThread.submit( RunnableBase.HowToRun.RUN_IN_SEPARATE_THREAD );
        }
        
        try
        {
            // TEST CODE (remove later): should throw an Exception
            new AMXConfigImplBase( null, null, null, AMXConfig.class, null, null );
            throw new Error( "AMXConfigLoader: AMXConfigImplBase did not throw an exception for a null j2eeType!!!" );
        }
        catch( Exception e )
        {
            // good!
        }
    }
    
    private final class AMXConfigLoaderThread extends RunnableBase
    {
        private final LinkedBlockingQueue<ConfigBean> mQueue;
        volatile boolean    mQuit = false;
        
        AMXConfigLoaderThread( final LinkedBlockingQueue<ConfigBean> queue )
        {
            super( "AMXConfigLoader.AMXConfigLoaderThread", null );
            mQueue = queue;
        }
        
        void quit() { mQuit = true; }
        
            protected void
        doRun() throws Exception
        {
            while ( ! mQuit )
            {
                final ConfigBean cb = mQueue.take();
                
                try 
                {
                    // recursive algorithm--parents need to be registered first,
                    // but might be later in the queue
                    if ( cb.getObjectName() == null )
                    {
                        registerConfigBeanAsMBean( cb );
                    }
                }
                catch( Throwable t )
                {
                    t.printStackTrace();
                }
            }
        }
    }
    /**
        Register the ConfigBean, first registering its parent, parent's parent, etc if not
        already present.
     */
        private void
    registerConfigBeanAsMBean( final ConfigBean cb )
    {
        if ( getAMXConfigInfo(cb) != null )
        {
            final ConfigBean parentCB = getActualParent(cb);
            if ( parentCB != null && parentCB.getObjectName() == null )
            {
                //debug( "REGISTER parent first: " + parentCB.getProxyType().getName() );
                registerConfigBeanAsMBean( parentCB );
                //debug( "REGISTERED parent: " + parentCB.getProxyType().getName() + " as " + JMXUtil.toString(parentCB.getObjectName()) );
            }
           final ObjectName objectName =  _registerConfigBeanAsMBean( cb, parentCB );
           assert cb.getObjectName() != null;
        }
        else
        {
            debug( "NOTE: ConfigBean has no @AMXConfigInfo: " + cb.getProxyType().getName() + " (IGNORING)");
        }
    }
    
        private AMXConfigInfo
    getAMXConfigInfo( final ConfigBean cb )
    {
        final Class<? extends ConfigBeanProxy> cbClass = cb.getProxyType();
        
        final AMXConfigInfo amxConfigInfo = cbClass.getAnnotation( AMXConfigInfo.class );
        
        return amxConfigInfo;
    }
    
         private AMXMBeanMetadata
    getAMXMBeanMetadata( final ConfigBean cb )
    {
        final Class<? extends ConfigBeanProxy> cbClass = cb.getProxyType();
        
        // check class itself first for metadata, if missing find it from the AMXConfigInfo instead
        AMXMBeanMetadata metadata    = cbClass.getAnnotation( AMXMBeanMetadata.class );
        if ( metadata == null )
        {
            // the default
            metadata = AMXConfigInfo.class.getAnnotation( AMXMBeanMetadata.class );
        }
        
        return metadata;
    }
    
    /**
        Parent must have been registered already.
     */
        private ObjectName
    _registerConfigBeanAsMBean(
        final ConfigBean cb,
        final ConfigBean parentCB )
    {
        final Class<? extends ConfigBeanProxy> cbClass = cb.getProxyType();
        
        ObjectName objectName = cb.getObjectName();
        if ( objectName != null )
        {
            throw new IllegalArgumentException( "ConfigBean " + cbClass.getName() + " already registered as " + objectName );
        }
        if ( parentCB != null && parentCB.getObjectName() == null )
        {
            throw new IllegalArgumentException( "ConfigBean parent " + parentCB.getProxyType().getName() +
                " must be registered first before child = " +cbClass.getName() );
        }
        
        final AMXConfigInfo amxConfigInfo = getAMXConfigInfo( cb );
        if ( amxConfigInfo == null )
        {
            throw new IllegalArgumentException( "ConfigBean has no @AMXConfigInfo: " + cbClass.getName() );
        }
        
        // don't process internal nodes like <configs>, <resources>, etc; these contain
        // nothing but child nodes.  Such nodes use AMXConfigVoid.
        if ( amxConfigInfo.amxInterface() != AMXConfigVoid.class )
        {
            final Class<? extends AMXConfig> amxInterface = amxConfigInfo.amxInterface();
        
            // if the specified interface is the base interface AMXConfig, then
            // the resulting interface is a combination of AMXConfig and the interface of the ConfigBean
            final boolean autoInterface = amxInterface == AMXConfig.class;
            final Class<?> supplementaryIntf = autoInterface ? (cbClass.isInterface() ? cbClass : null) : null;
            
            final AMXMBeanMetadata metadata        = getAMXMBeanMetadata(cb);
            
            // debug( "Preparing ConfigBean for registration with ObjectNameInfo = " + objectNameInfo.toString() + ", AMXMBeanMetaData = " + metadata );

            objectName = buildObjectName( cb, amxConfigInfo );
        
            objectName  = createAndRegister( cb, amxInterface, supplementaryIntf, objectName );
            debug( "REGISTERED MBEAN: " + JMXUtil.toString(objectName) + " ===> USING " +
                " AMXConfigInfo = " + amxConfigInfo.toString() +
                ", AMXMBeanMetaData = " + metadata + "\n");
        }
        
        return objectName;
    }
    
    /**
		@return the fully qualified type as required by AMX.FULL_TYPE
	 */
		protected static String
	getFullType( final ConfigBean cb, final ObjectName proposedObjectName )
	{
        String fullType = "";
        if ( cb != null )
        {
            ObjectName objectName = (proposedObjectName != null) ? proposedObjectName : cb.getObjectName();
            final String j2eeType = objectName.getKeyProperty(AMX.J2EE_TYPE_KEY);
            
            final ConfigBean parent = getActualParent( cb );
            if ( parent == null )
            {
                fullType = j2eeType;
            }
            else
            {
                fullType = getFullType(parent, null) + "." + j2eeType;
            }
            //debug( "Full type for " + cb.getObjectName() + " = " + fullType );
        }
        
		return( fullType );
	}

    private ObjectName createAndRegister(
        final ConfigBean cb,
        final Class<? extends AMXConfig>  amxInterface,
        final Class<?>  supplementaryIntf,
        final ObjectName objectNameIn )
    {
        ObjectName  objectName = objectNameIn;
        
        final String j2eeType = objectNameIn.getKeyProperty(AMX.J2EE_TYPE_KEY);
        final String fullType = getFullType(cb, objectName );

        //debug( "Full type for " + objectNameIn + " = " + fullType );
        
        final Delegate delegate = new DelegateToConfigBeanDelegate( cb );
        ObjectName parentObjectName = getActualParentObjectName( cb );
        
        if ( parentObjectName == null  )
        {
            if ( amxInterface == com.sun.appserv.management.config.DomainConfig.class )
            {
                parentObjectName = ObjectNames.getInstance().getDomainRootObjectName();
            }
            else
            {
                //debug( "WARNING: All ConfigBeans must have a parent!  No parent for " + cb.getProxyType().getName() );
                throw new IllegalArgumentException( "All AMXConfig MBeans must have a parent!  No parent for " + cb.getProxyType().getName() );
            }
        }
        
        final AMXConfigImplBase impl = new AMXConfigImplBase( j2eeType, fullType, parentObjectName, amxInterface, supplementaryIntf, delegate );
        
        try
        {
            final ObjectInstance instance = mMBeanServer.registerMBean( impl, objectNameIn );
            objectName = instance.getObjectName();
            cb.setObjectName( objectName );
        }
        catch( final JMException e )
        {
            debug( ExceptionUtil.toString(e) );
            objectName = null;
        }
        return objectName;
    }

        private String
    getJ2EETypeField( final Class<? extends AMXConfig>  amxInterface )
    {
        String j2eeType = null;
        try {
            j2eeType	= (String)ClassUtil.getFieldValue( amxInterface, "J2EE_TYPE" );
        }
        catch( Exception e )
        {
            // this is NOT OK: should be specified if something other than generic
            //debug( "No J2EE_TYPE field found in " + amxInterface.getName() );
            throw new IllegalArgumentException(e);
        }
        return j2eeType;
    }
        private String
    getJ2EEType(
        final ConfigBean cb,
        final AMXConfigInfo info)
    {
        final Class<? extends AMXConfig> amxInterface = info.amxInterface();
        
        String j2eeType = null;
        // if a specific AMX interface was specified (not the base interface AMXConfig), 
        // use its J2EE_TYPE field preferentially
        if ( amxInterface != AMXConfig.class )
        {
            j2eeType = getJ2EETypeField( amxInterface );
        }
        if ( j2eeType == null )
        {
            // Use the value from the annotation
            j2eeType = info.j2eeType();
            
            // if the value is empty, derive one from the fully-qualified interface name
            if ( j2eeType.length() == 0 )
            {
                // don't allow "." in the type; it will confuse the "full type" attribute
                final String configInterfaceName = cb.getProxyType().getName().replace(".", "_");
                j2eeType = XTypes.PREFIX + "CFG-" + configInterfaceName;
                debug( "Using DERIVED j2eeType of " + j2eeType + " for " + configInterfaceName );
            }
        }
        assert j2eeType != null && j2eeType.startsWith( XTypes.PREFIX );
        
        if ( j2eeType == null )
        {
            throw new RuntimeException( "AMXConfigLoader.getJ2EEType: j2eeType is null" );
        }
        if ( ! j2eeType.startsWith( XTypes.PREFIX ) )
        {
            throw new RuntimeException( "AMXConfigLoader.getJ2EEType: j2eeType just start with " + XTypes.PREFIX);
        }
        
        return j2eeType;
    }
    
        private String
    getName(
        final ConfigBean cb,
        final AMXConfigInfo info)
    {
        String name = info.singleton() ? AMX.NO_NAME : cb.rawAttribute( info.nameHint() );
        
        if ( name == null )
        {
            name = "BUG_NO_NAME_AVAILABLE";
        }
        return name;
    }
    
        private ObjectName
    buildObjectName(
        final ConfigBean cb,
        final AMXConfigInfo info )
    {
        final String j2eeType = getJ2EEType( cb, info );
        final String name     = getName( cb, info );
        
        String parentProps = "";
        String domain = AMX.JMX_DOMAIN;
        final ConfigBean parent = getActualParent(cb);
        if ( parent != null )
        {
            final ObjectName parentObjectName = parent.getObjectName();
            
            if ( parentObjectName != null )
            {
                domain = parentObjectName.getDomain();
                
                // a child's ObjectName is parentJ2EEType=parentName,<all other parent properties>
                final String ancestorProps = Util.getAdditionalProps( parentObjectName );
                final String parentProp     = info.omitAsAncestorInChildObjectName() ? "" : Util.getSelfProp( parentObjectName );
                
                parentProps = Util.concatenateProps( parentProp, ancestorProps );
            }
        }
        
        final String requiredProps = Util.makeRequiredProps( j2eeType, name );
        final String allProps = Util.concatenateProps( requiredProps, parentProps );
        
        final ObjectName objectName = Util.newObjectName( domain, allProps );
        return objectName;
    }
}













































