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
package org.glassfish.admin.amx.config;

import com.sun.appserv.management.base.AMXAttributes;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import org.glassfish.admin.amx.mbean.DelegateBase;
import org.glassfish.admin.amx.util.AMXConfigInfoResolver;
import org.glassfish.api.amx.AMXConfigInfo;
import org.jvnet.hk2.config.*;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import java.beans.PropertyChangeEvent;
import java.util.*;

import com.sun.appserv.management.util.misc.TypeCast;

/**
	Delegate which delegates to another MBean.
 */
public final class DelegateToConfigBeanDelegate extends DelegateBase
{
	private final ConfigBean mConfigBean;
    private final NameMapping mNameMapping;
    
    private static void debug( final String s ) { System.out.println(s); }
	
        private static AMXConfigInfoResolver
    getAMXConfigInfoResolver( final ConfigBean cb )
    {
        final Class<? extends ConfigBeanProxy> proxyClass = cb.getProxyType();
        final AMXConfigInfo amxConfigInfo = proxyClass.getAnnotation( AMXConfigInfo.class );
        if ( amxConfigInfo == null )
        {
            throw new IllegalArgumentException();
        }
        return new AMXConfigInfoResolver( amxConfigInfo );
    }
    
        private static String
    getJ2EEType( final ConfigBean cb )
    {
        return getAMXConfigInfoResolver(cb).j2eeType();
    }
    
		public
	DelegateToConfigBeanDelegate(
        final ConfigBean configBean )
	{
		super( "DelegateToConfigBeanDelegate." + configBean.toString() );
		
		mConfigBean	= configBean;
        mNameMapping    = NameMapping.getInstance( getJ2EEType(configBean) );
	}
	
    public ConfigBean getConfigBean() { return mConfigBean; }
     
		public boolean
	supportsAttribute( final String attrName )
	{
        final String xmlName = getXMLName(attrName);
        
        //debug( "DelegateToConfigBeanDelegate.supportsAttribute: " + attrName + " => " + xmlName );
        return xmlName != null;
	}
    
    /**
        Belongs in HK2 itself (Dom.java), but here until versioning can be worked out.
     */
        protected ConfigModel.Property
    getConfigModel_Property( final String xmlName ) {
        final ConfigModel.Property cmp = mConfigBean.model.findIgnoreCase(xmlName);
        if (cmp == null) {
            throw new IllegalArgumentException( "Illegal name: " + xmlName );
        }
        return cmp;
    }
    
    public boolean isLeaf( final String xmlName ) {
        return getConfigModel_Property(xmlName).isLeaf();
    }
    
    public boolean isCollection( final String xmlName ) {
        return getConfigModel_Property(xmlName).isCollection();
    }

    
    /**
        Utilize AMXConfigInfo for arbitrary name mappings, at least nameHint()
     */
        private String
    smartAttrNameFind( final String amxName )
    {
        String xmlName = null;
        // look for nameHint() in annotation
       if ( amxName.equals( AMXAttributes.ATTR_NAME ) )
       {
            final AMXConfigInfoResolver info = getAMXConfigInfoResolver( mConfigBean );
            final String hint = info.nameHint();
            if ( hint != null && hint.length() != 0 )
            {
                //debug( "smartNameFind: mapped " + amxName + " to " + hint + " for " + info.amxInterface().getName() ); 
                xmlName = hint;
            }
        }
       return xmlName;
    }
    
    /**
        Get the XML attribute name corresponding to the AMX attribute name.
     */
        private final String
    getXMLName( final String amxName )
    {
        String xmlName = mNameMapping.getXMLName( amxName );
        if ( xmlName == null )
        {
            xmlName = smartAttrNameFind( amxName );
            if ( xmlName == null )
            {
                final Set<String> xmlNames = mConfigBean.getAttributeNames();
                //debug( "Attribute names: " + CollectionUtil.toString( xmlNames ) );
                
                xmlName = mNameMapping.matchAMXName( amxName, xmlNames );
                //debug( "Matched: " + amxName + " => " + xmlName );
                if ( xmlName == null )
                {
                    final Set<String> leafNames = mConfigBean.getLeafElementNames();
                    xmlName = mNameMapping.matchAMXName( amxName, leafNames );
                    //debug( "Matched leaf element names: " + CollectionUtil.toString(leafNames) + " = " + xmlName );
                }
            }
            else
            {
                mNameMapping.pairNames( amxName, xmlName );
            }
        }
        
        //debug( "getXMLName " + amxName + " => " + xmlName );
        //debug( "amxAttrNameToConfigBeanName: resolved as : " + xmlName );
        return xmlName;
    }
    
    /**
        Get an AttrInfo based on the AMX attribute name.
     */
        private AttrInfo
    getAttrInfo_AMX( final String amxName )
    {
        final String xmlName = getXMLName(amxName);

        final boolean isLeaf       = isLeaf(xmlName);
        final boolean isCollection = isCollection(xmlName);

        return new AttrInfo( amxName, getXMLName( amxName), isLeaf, isCollection );
    }
            
		public final Object
	getAttribute( final String amxName )
		throws AttributeNotFoundException
	{
        final AttrInfo info = getAttrInfo_AMX( amxName );
        
        Object result = null;
        final String xmlName = info.xmlName();
        debug( "DelegateToConfigBeanDelegate.getAttribute: attrInfo: " + info );
        if ( info.isCollection() )
        {
            final List<?> leafElementsX = mConfigBean.leafElements(xmlName);
            if ( leafElementsX != null ) {
                // verify that it is List<String> -- no other types are supported in this way
                final List<String> leafElements = TypeCast.checkList( leafElementsX, String.class );
                result = CollectionUtil.toArray( leafElements, String.class);
            }
        }
        else
        {
            // all plain attributes are 'String'
            result = (String)mConfigBean.rawAttribute( xmlName );
        }
        
        debug( "Attribute " + amxName + " has class " + ((result == null) ? "null" : result.getClass()) );
        return result;
	}
    
    
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
    
        private Object
    autoStringify( final Object o )
    {
        Object result = o;
        
        if ( o != null &&
            ((o instanceof Boolean) || (o instanceof Integer) || (o instanceof Long)) )
        {
            result = "" + o;
        }
        
        return result;
    }
    
		public AttributeList
	setAttributes( final AttributeList attrsIn, final Map<String,Object> oldValues )
	{
        oldValues.clear();
        
        // note that attributeListToStringMap() auto-converts types to 'String' which is desired here
        final Map<String, Object> amxAttrs = JMXUtil.attributeListToValueMap( attrsIn );
        
        /*
        // auto convert certain special types such as String[] to String
        for( final String key : amxAttrs.keySet() )
        {
            final Object value = amxAttrs.get(key);
            
            String valueString = "" + value;
            amxAttrs.put( key, valueString );
        }
        */
        
        // now map the AMX attribute names to xml attribute names
        final Map<String,String> xmlAttrs = new HashMap<String,String>();
        for( final String amxAttrName : amxAttrs.keySet() )
        {
            final String xmlName = getXMLName(amxAttrName);
            if ( xmlName != null )
            {
                final Object valueIn = amxAttrs.get(amxAttrName);
                final Object value = autoStringify( valueIn );
                if ( value != valueIn )
                {
                    debug( "Attribute " + amxAttrName + " auto converted from " +
                        valueIn.getClass().getName() + " to " + value.getClass().getName() );
                }
                
                if ( ! (value instanceof String) )
                {
                    debug( "Attribute " + amxAttrName + " is not a String, IGNORING" );
                }
                else
                {
                    xmlAttrs.put( xmlName, (String)value);
                }
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
}











