package org.glassfish.admin.amx;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.JMException;
import javax.management.ObjectInstance;

import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.component.CageBuilder;

import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

/**
 * @author llc
 */
@Service //(name="AMXConfigRegistrar")
public final class AMXConfigRegistrar implements CageBuilder
{
    @Inject
    MBeanServer mMBeanServer;
    
    @SuppressWarnings("unchecked")
    final ConfigBean asConfigBean( final Object o )
    {
        return (o instanceof ConfigBean) ? (ConfigBean)o : null;
    }
    
        public
    AMXConfigRegistrar()
    {
        debug( "#### AMXConfigRegistrar.AMXConfigRegistrar  #####" );
    }
    
    private static void debug( final String s ) { System.out.println(s); }
    
    public void onEntered(Inhabitant<?> inhabitant)
    {
        debug( "AMXConfigRegistrar: inhabitant: " + inhabitant );
        final ConfigBean cb = asConfigBean(inhabitant);
        if ( cb != null )
        {
            registerConfigBean( cb );
        }
    }
    
    /**
     */
        protected ObjectName
    registerConfigBean( final ConfigBean cb )
    {
        ObjectName objectName = cb.getObjectName();
        if ( objectName != null )
        {
            throw new IllegalArgumentException( "ConfigBean " + cb + " already registered as " + objectName );
        }
        
        final Class<? extends ConfigBeanProxy> configuredClass = cb.getProxyType();
        // should be getting @AMXInfo, and using meta annotation
        final AMXConfigInfo amxConfigInfo = configuredClass.getAnnotation( AMXConfigInfo.class );
        if ( amxConfigInfo == null )
        {
            throw new IllegalArgumentException( "Missing @AMXConfigInfo" );
        }
        
        // check class itself first for metadata, if missing find it from the AMXConfigInfo instead
        AMXMBeanMetadata metadata    = configuredClass.getAnnotation( AMXMBeanMetadata.class );
        if ( metadata == null )
        {
            // the default
            metadata = AMXConfigInfo.class.getAnnotation( AMXMBeanMetadata.class );
        }
        
        final AMXObjectNameInfo objectNameInfo = configuredClass.getAnnotation( AMXObjectNameInfo.class );
        if ( objectNameInfo == null )
        {
            throw new IllegalArgumentException( "Missing @AMXObjectNameInfo" );
        }
        
    debug( "Preparing ConfigBean for registration with ObjectNameInfo = " + objectNameInfo.toString() + ", AMXMBeanMetaData = " + metadata );

        objectName = buildObjectName( cb, objectNameInfo );
    
        try
        {
            final ObjectInstance instance = mMBeanServer.registerMBean( new Dummy(cb), objectName );
            objectName = instance.getObjectName();
            cb.setObjectName( objectName );
            debug( "REGISTERED MBEAN: " + JMXUtil.toString(objectName) );
        }
        catch( final JMException e )
        {
            debug( ExceptionUtil.toString(e) );
        }

        return objectName;
    }
    
        ObjectName
    buildObjectName(
        final ConfigBean b,
        final AMXObjectNameInfo info)
    {
        final String name = b.rawAttribute( info.nameHint() );
        
        final String nameString = "amx:j2eeType=" + info.j2eeType() + ",name=" + name;
        
        return JMXUtil.newObjectName( nameString );
    }
}


















