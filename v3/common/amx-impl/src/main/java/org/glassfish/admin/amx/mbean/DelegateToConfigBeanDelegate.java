/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package org.glassfish.admin.amx.mbean;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanInfo;
import javax.management.AttributeList;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.ReflectionException;

import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.config.TransactionListener;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Dom;


import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.PropertyConfig;

import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import com.sun.appserv.management.util.jmx.JMXUtil;

import com.sun.appserv.management.annotation.AMXConfigInfo;



/**
	Delegate which delegates to another MBean.
 */
public final class DelegateToConfigBeanDelegate extends DelegateBase
{
	private final ConfigBean mConfigBean;
    
    private static void debug( final String s ) { System.out.println(s); }
	
		public
	DelegateToConfigBeanDelegate(
        final ConfigBean configBean )
	{
		super( "DelegateToConfigBeanDelegate." + configBean.toString() );
		
		mConfigBean	= configBean;
	}
	
     
		public boolean
	supportsAttribute( final String attrName )
	{
        final String xmlName = getXMLName(attrName);
        
        //debug( "DelegateToConfigBeanDelegate.supportsAttribute: " + attrName + " => " + xmlName );
        return xmlName != null;
	}
    
    /**
        Get the XML attribute name corresponding to the AMX attribute name.
     */
        private final String
    getXMLName( final String amxName )
    {
       //debug( "getXMLName " + amxName );
       
        String xmlName = NameMapping.getXMLName( amxName );
        if ( xmlName == null )
        {
            final Set<String> xmlNames = mConfigBean.getAttributeNames();
            //debug( "Attribute names: " + CollectionUtil.toString( xmlNames ) );
            
            xmlName = NameMapping.matchAMXName( amxName, xmlNames );
            //debug( "Matched: " + amxName + " => " + xmlName );
        }
        
        //debug( "getXMLName " + amxName + " => " + xmlName );
        //debug( "amxAttrNameToConfigBeanName: resolved as : " + xmlName );
        return xmlName;
    }
    
            
		public final Object
	getAttribute( final String attrName )
		throws AttributeNotFoundException
	{
        //debug( "DelegateToConfigBeanDelegate.getAttribute: " + attrName );
        final String xmlName = getXMLName(attrName);
        
       final Object result = mConfigBean.rawAttribute( xmlName );
       
       debug( "Attribute " + attrName + " has class " + result.getClass() );
       return result;
	}
    
    private static final String[]   SINGLE_STRING_SIG   = new String[] { String.class.getName() };
    
    @Override
        protected final String
    _getDefaultValue( final String name )
        throws AttributeNotFoundException
    {
        throw new AttributeNotFoundException( name );
    }

    private static final class MyTransactionListener implements TransactionListener
    {
        private static final List<PropertyChangeEvent> EMPTY = Collections.emptyList();
        private volatile List<PropertyChangeEvent> mChangeEvents = EMPTY;
        
        MyTransactionListener() {}
        
        public void transactionCommited(List<PropertyChangeEvent> changes) {
            if ( mChangeEvents != EMPTY )
            {
                throw new IllegalStateException( "can commit only once!" );
            }
            
            mChangeEvents = changes;
        }
        List<PropertyChangeEvent>  getChangeEvents() { return mChangeEvents; }
    };

    /**
        Make a Map keyed by the property name of the PropertyChangeEvent, verifying that each
        name is non-null.
     */
        private  Map<String,PropertyChangeEvent>
    makePropertyChangeEventMap( final List<PropertyChangeEvent> changeEvents )
    {
        final Map<String,PropertyChangeEvent>   m = new HashMap<String,PropertyChangeEvent>();
        
        for( final PropertyChangeEvent changeEvent : changeEvents )
        {
            if ( changeEvent.getPropertyName() == null )
            {
                throw new IllegalArgumentException( "PropertyChangeEvent property names must be specified" );
            }
            
            m.put( changeEvent.getPropertyName(), changeEvent );
        }
        return m;
    }
    
		public AttributeList
	setAttributes( final AttributeList attrsIn, final Map<String,Object> oldValues )
	{
        oldValues.clear();
        
        // note that attributeListToStringMap() auto-converts types to 'String' which is desired here
        final Map<String, String> amxAttrs = JMXUtil.attributeListToStringMap( attrsIn );
        
        // now map the AMX attribute names to xml attribute names
        final Map<String,String> xmlAttrs = new HashMap<String,String>();
        for( final String amxAttrName : amxAttrs.keySet() )
        {
            final String xmlName = getXMLName(amxAttrName);
            if ( xmlName != null )
            {
                xmlAttrs.put( xmlName, amxAttrs.get(amxAttrName));
            }
        }
        
        final Map<ConfigBean, Map<String, String>> changes = new HashMap<ConfigBean, Map<String, String>>();
        changes.put( mConfigBean, xmlAttrs );
        
        debug( "DelegateToConfigBeanDelegate.setAttributes(): " + attrsIn.size() + " attributes: {" +
            CollectionUtil.toString(amxAttrs.keySet()) + "} mapped to xml names {" + CollectionUtil.toString(xmlAttrs.keySet()) + "}");
        
        final MyTransactionListener  myListener = new MyTransactionListener();
        Transactions.get().addTransactionsListener(myListener);
            
        // results should contain only those that succeeded which will be all or none
        // depending on whether the transaction worked or not
        final AttributeList successfulAttrs = new AttributeList();
        try
        {
            ConfigSupport.apply( changes );
            // use 'attrsIn' vs 'attrs' in case not all values are 'String'
            successfulAttrs.addAll( attrsIn );
        }
        catch( final TransactionFailure tf )
        {
            // empty results -- no Exception should be thrown per JMX spec
            debug( ExceptionUtil.toString(tf) );
        }
        finally
        {
            Transactions.get().waitForDrain();
            Transactions.get().removeTransactionsListener(myListener);
        }
        
        if ( successfulAttrs.size() != 0 )
        {
            // verify that the size of the PropertyChangeEvent list matches
            final List<PropertyChangeEvent> changeEvents = myListener.getChangeEvents();
            if ( successfulAttrs.size() != changeEvents.size() )
            {
                throw new IllegalStateException( "List<PropertyChangeEvent> does not match the number of Attributes" );
            }
            
            //
            // provide details on old values for the caller. Note that config always returns
            // type 'String' which no ability to map back to 'Integer', etc, so the MBeanInfo info
            // of the MBean should not be using anything but String.
            // 
            final Map<String,PropertyChangeEvent> eventsMap = makePropertyChangeEventMap( changeEvents );
            final Map<String, String> attrsS = JMXUtil.attributeListToStringMap( successfulAttrs );
            
            // supply all the old values to caller using the AMX attribute name
            for( final String amxAttrName : attrsS.keySet() )
            {
                final PropertyChangeEvent changeEvent = eventsMap.get( getXMLName( amxAttrName ) );
                oldValues.put( amxAttrName, changeEvent.getOldValue() );
            }
        }
    
		return successfulAttrs;
	}
	
		public MBeanInfo
	getMBeanInfo()
	{
		return( null );
	}
    
		private void
	delegateFailed( final Throwable t )
	{
		if ( getOwner() != null )
		{
			getOwner().delegateFailed( t );
		}
	}

	/**
	 */
		public final Object
	invoke(
		String 		operationName,
		Object[]	args,
		String[]	types )
	{
        throw new RuntimeException( "invoke() not yet implemented" );
	}
    

//-------------------------------------------------------------------
// test/exploratory code for create() methods
    
    /**
        Find the @Configured interface that should be instantiated for the corresponding j2eeType.
     */
        private Class<? extends ConfigBeanProxy> 
    getChildInterface( final String j2eeType )
    {
        Class<? extends ConfigBeanProxy>  intf = null;
        
        debug( "NO CODE YET TO FIND CHILD INTERFACE" );
        
        if ( intf == null )
        {
            throw new IllegalArgumentException( "Unknown j2eeType for creation: " + j2eeType );
        }
        
        final AMXConfigInfo configInfo = intf.getAnnotation( AMXConfigInfo.class );
        if ( configInfo == null )
        {
            throw new IllegalArgumentException( "no AMXConfigInfo found for " + intf.getName() );
        }
        
        return intf;
    }
    
    private static final class ChildMaker implements SingleConfigCode<ConfigBeanProxy>
    {
        private final Class<? extends ConfigBeanProxy> mIntf;
        private final Map<String,String>    mAttrs;
        
        private ConfigBean  mChild = null;
        
        public ChildMaker(
            final Class<? extends ConfigBeanProxy>  intf,
            final Map<String,String> attrs)
        {
            mIntf  = intf;
            mAttrs = attrs;
        }
        
        public Object run(ConfigBeanProxy param) throws PropertyVetoException, TransactionFailure
        {
            final ConfigBeanProxy proxy = ConfigSupport.createChildOf( param, mIntf );
            
            mChild = (ConfigBean)Dom.unwrap( proxy );
            
            for( final String attrName : mAttrs.keySet() )
            {
                mChild.attribute( attrName, mAttrs.get(attrName) );
            }
            return mChild;
        }
        
        public ConfigBean getNewborn() { return mChild; }
    }
    
        private ConfigBean
    createChild(
        final Class<? extends ConfigBeanProxy> intf,
        final Map<String,String>  attrs )
    {
        final AMXConfigInfo configInfo = intf.getAnnotation( AMXConfigInfo.class );
        
        final ChildMaker  mc = new ChildMaker( intf, attrs );
        
        try
        {
            ConfigSupport.apply( mc, mConfigBean.createProxy() );
        }
        catch( TransactionFailure e )
        {
            debug( ExceptionUtil.toString(e) );
            throw new RuntimeException(e);
        }
        
        final ConfigBean child = mc.getNewborn();

        return child;
    }


        public ConfigBean
    createChild(
        final String j2eeType,
        final Object... args )
    {
        final Class<? extends ConfigBeanProxy> intf = getChildInterface( j2eeType );
        
        final Map<String,String> attrs = new HashMap<String,String>();
        // attrs must be filled in with attribute names mapped to values...
        
        return createChild( intf, attrs );
    }
}











