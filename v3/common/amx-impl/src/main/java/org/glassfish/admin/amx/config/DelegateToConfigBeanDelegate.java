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
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ListUtil;
import org.glassfish.admin.amx.mbean.DelegateBase;
import org.glassfish.admin.amx.util.AMXConfigInfoResolver;
import org.glassfish.api.amx.AMXConfigInfo;
import org.jvnet.hk2.config.*;

import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.jvnet.hk2.config.*;

import com.sun.appserv.management.util.misc.TypeCast;
import static com.sun.appserv.management.config.CollectionOp.*;


/**
	Delegate which delegates to another MBean.
 */
public final class DelegateToConfigBeanDelegate extends DelegateBase
{
	private final ConfigBean        mConfigBean;
    private final NameMappingHelper mNameMappingHelper;
    
    private static void debug( final String s ) { System.out.println(s); }
	    
		public
	DelegateToConfigBeanDelegate(
        final ConfigBean configBean )
	{
		super( "DelegateToConfigBeanDelegate." + configBean.toString() );
		
		mConfigBean	= configBean;
        
        mNameMappingHelper = new NameMappingHelper( configBean );
	}
    
		public boolean
	supportsAttribute( final String amxAttrName )
	{
        final String xmlName = mNameMappingHelper.getXMLName(amxAttrName);
        //debug( "DelegateToConfigBeanDelegate.supportsAttribute: " + attrName + " => " + xmlName );
        return xmlName != null;
	}
	
    public ConfigBean getConfigBean() { return mConfigBean; }
                   
		public final Object
	getAttribute( final String amxName )
		throws AttributeNotFoundException
	{
        final AttrInfo info = mNameMappingHelper.getAttrInfo_AMX( amxName );
        
        Object result = null;
        final String xmlName = info.xmlName();
        //debug( "DelegateToConfigBeanDelegate.getAttribute: attrInfo: " + info );
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
        
       // debug( "Attribute " + amxName + " has class " + ((result == null) ? "null" : result.getClass()) );
        return result;
	}
    
    private static final class MyTransactionListener implements TransactionListener
    {
        private final List<PropertyChangeEvent> mChangeEvents = new ArrayList<PropertyChangeEvent>();
        private final ConfigBean    mTarget;
        
        MyTransactionListener( final ConfigBean target ) { mTarget = target;}
        
        public void transactionCommited(List<PropertyChangeEvent> changes) {
            // include only events that match the desired config bean; other transactions
            // could generate events on other ConfigBeans. For that matter, it's unclear
            // why more than one transaction on the same ConfigBean couldn't be "heard" here.
            for( final PropertyChangeEvent event : changes )
            {
                final Object source = event.getSource();
                if ( source instanceof ConfigBeanProxy )
                {
                    final Dom dom = Dom.unwrap( (ConfigBeanProxy)source );
                    if ( dom instanceof ConfigBean )
                    {
                        if ( mTarget == (ConfigBean)dom )
                        {
                            mChangeEvents.add( event );
                        }
                    }
                }
            }
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
    
    private void joinTransaction( final Transaction t, final WriteableView writeable ) 
        throws TransactionFailure
    {
        if ( ! writeable.join(t) )
        {
            t.rollback();
            throw new TransactionFailure("Cannot enlist " + writeable.getProxyType() + " in transaction",null);
        }
    } 
        
        private static void
    commit( final Transaction t )
        throws TransactionFailure
    {
        try
        {
            t.commit();
        }
        catch ( final RetryableException e)
        {
            debug("Retryable...");
            t.rollback();
            throw new TransactionFailure(e.getMessage(), e);
        }
        catch ( final TransactionFailure e) {
            debug("failure, not retryable...");
            t.rollback();
            throw e;
        }
    }
    
        static <T extends ConfigBeanProxy> WriteableView
     getWriteableView(final T s, final ConfigBean sourceBean)
        throws TransactionFailure {
        final WriteableView f = new WriteableView(s);
        if (sourceBean.getLock().tryLock()) {
            return f;
        }
        throw new TransactionFailure("Config bean already locked " + sourceBean, null);
    }
    
    private static Type getCollectionGenericType() 
        throws NoSuchMethodException
    {
        return ConfigSupport.class.getDeclaredMethod("defaultPropertyValue", null).getGenericReturnType();
    }    
    
    private static boolean isCollectionCmd( final String s )
    {
        return s != null && s.startsWith(COLLECTION_CMD_PREFIX) && s.endsWith(COLLECTION_CMD_SUFFIX);
    }
            
    private void apply(
        final ConfigBean cb,
        final Map<String,Object> changes )
        throws TransactionFailure
    {
        final Transaction t = new Transaction();
        final Class<? extends ConfigBeanProxy> intf = cb.getProxyType();
        final ConfigBeanProxy readableView = cb.getProxy( intf );
        final WriteableView writeable = getWriteableView(readableView, cb );
        try
        {
            joinTransaction( t, writeable);
                
            for ( final String xmlName : changes.keySet() )
            {
                final Object value = changes.get(xmlName);
                final ConfigModel.Property prop = mNameMappingHelper.getConfigModel_Property(xmlName);

                if ( prop.isCollection() )
                {
                    try
                    {
                        final Object o = writeable.getter(prop, getCollectionGenericType());
                        final List<String> existingValuesList = TypeCast.checkList( TypeCast.asList(o), String.class);
                        
                        // make a working copy
                        final List<String> workList = new ArrayList(existingValuesList);
                        
                        // single string or List<String> or String[] are all mapped to a list
                        final List<String> argValues = ListUtil.asStringList( value );
                        if ( argValues.size() == 0 ) continue;
                                                
                        // check for command on what to do -- first argument could be a command
                        final String first = argValues.get(0);
                        final String cmd   = isCollectionCmd(first) ? first : COLLECTION_OP_ADD;
                        if ( cmd.equals( COLLECTION_OP_REPLACE ) )
                        {
                            workList.clear();
                            workList.addAll( argValues );
                        }
                        else if ( cmd.equals( COLLECTION_OP_REMOVE ) )
                        {
                            workList.removeAll( argValues );
                        }
                        else if ( cmd.equals( COLLECTION_OP_ADD ) )
                        {
                            // eliminate duplicates for now unless there is a good reason to allow them
                            argValues.removeAll( workList );
                            
                            // add in any that are not duplicates
                            workList.addAll( argValues );
                        }
                        else
                        {
                            throw new IllegalArgumentException(cmd);
                        }
                        
                        // the existing list does not support clear() or removeAll()
                        // and it's broken if we remove anything. Arggg....
                        /*
                        while ( existingValuesList.size() != 0 )
                        {
                            existingValuesList.remove( existingValuesList.get( existingValuesList.size() - 1 ) );
                        }
                        */
                        
                       // existingValuesList.removeAll( workList );
                        existingValuesList.addAll( workList );
                    }
                    catch ( final NoSuchMethodException e)
                    {
                        throw new TransactionFailure("Unknown property name " + xmlName + " on " + intf.getName(), null);                        
                    }
                }
                else if ( value == null || (value instanceof String) )
                {
                    writeable.setter( prop, value, String.class);
                }
                else
                {
                    throw new TransactionFailure( "Illegal data type for attribute " + xmlName + ": " + value.getClass().getName() );
                }
            }
            
            commit( t );
        }
        finally
        {
            cb.getLock().unlock();
        }
    }


		public AttributeList
	setAttributes( final AttributeList attrsIn, final Map<String,Object> oldValues )
	{
        oldValues.clear();
        
        // now map the AMX attribute names to xml attribute names
        final Map<String, Object> amxAttrs = JMXUtil.attributeListToValueMap( attrsIn );
        final Map<String,Object>  notMatched = new HashMap<String,Object>();
        final Map<String,Object>  xmlAttrs = mNameMappingHelper.mapNamesAndValues( amxAttrs, notMatched);
        
        if ( notMatched.keySet().size() != 0 )
        {
            debug( "setAttributes: failed to map these AMX attributes: {" + CollectionUtil.toString( notMatched.keySet() ) + "}" );
        }
        
        final AttributeList successfulAttrs = new AttributeList();
        
        if ( xmlAttrs.size() != 0 )
        {
            /*
            SAMPLE CODE, but what method to use?
            List<ConfigSupport.AttributeChanges> changes = new ArrayList<ConfigSupport.AttributeChanges>();
            String[] values = { "-Xmx512m", "-RFtrq", "-Xmw24" };
            ConfigSupport.MultipleAttributeChanges multipleChanges = new ConfigSupport.MultipleAttributeChanges("jvm-options", values );
            profilerChanges.add(multipleChanges);
            ConfigSupport.createAndSet( mConfigBean, mConfigBean.getProxyType(), changes);
            */
            
            debug( "DelegateToConfigBeanDelegate.setAttributes(): " + attrsIn.size() + " attributes: {" +
                CollectionUtil.toString(amxAttrs.keySet()) + "} mapped to xml names {" + CollectionUtil.toString(xmlAttrs.keySet()) + "}");
            
            final MyTransactionListener  myListener = new MyTransactionListener( mConfigBean );
            Transactions.get().addTransactionsListener(myListener);
                
            // results should contain only those that succeeded which will be all or none
            // depending on whether the transaction worked or not
            try
            {
                apply( mConfigBean, xmlAttrs );
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
        
            // determine later the best way to handle AttributeChangeNotification
            // It can get ugly at this level; the config code will issue a different event
            // for every single <jvm-options> element (for example)
            /*
            if ( successfulAttrs.size() != 0 )
            {
                // verify that the size of the PropertyChangeEvent list matches
                final List<PropertyChangeEvent> changeEvents = myListener.getChangeEvents();
                if ( successfulAttrs.size() != changeEvents.size() )
                {
                    throw new IllegalStateException( "List<PropertyChangeEvent> size=" + changeEvents.size() +
                        " does not match the number of Attributes, size = " + successfulAttrs.size() );
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
                    final PropertyChangeEvent changeEvent = eventsMap.get( mNameMappingHelper.getXMLName( amxAttrName ) );
                    oldValues.put( amxAttrName, changeEvent.getOldValue() );
                }
            }
            */
        }
    
		return successfulAttrs;
	}
}











