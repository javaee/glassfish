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
import com.sun.appserv.management.util.misc.StringUtil;

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
import static com.sun.appserv.management.config.AnonymousElementList.*;


/**
	Delegate which delegates to another MBean.
 */
public final class DelegateToConfigBeanDelegate extends DelegateBase
{
	private final ConfigBean        mConfigBean;
    private final NameMappingHelper mNameMappingHelper;
    private final ConfiguredHelper  mConfiguredHelper;
    
    private static void debug( final String s ) { System.out.println(s); }
    
		public
	DelegateToConfigBeanDelegate(
        final ConfigBean configBean )
	{
		super( "DelegateToConfigBeanDelegate." + configBean.toString() );
		
		mConfigBean	= configBean;
        
        mNameMappingHelper = new NameMappingHelper( configBean );
        mConfiguredHelper = ConfiguredHelperRegistry.getInstance( configBean.getProxyType() );
	}
    
		void
	initNameMapping( final String[] amxAttrNames )
	{
        mNameMappingHelper.initNameMapping( amxAttrNames );
    }
    
		public boolean
	supportsAttribute( final String amxAttrName )
	{
        final String xmlName = mNameMappingHelper.getXMLName(amxAttrName);
        //debug( "DelegateToConfigBeanDelegate.supportsAttribute: " + attrName + " => " + xmlName );
        return xmlName != null;
	}
    
    /**
        Name *must* be an AMX attribute name.
     */
        public AttrInfo
    getAttrInfo_AMX( final String attrName )
    {
        return mNameMappingHelper.getAttrInfo_AMX(attrName);
    }
	
    public ConfigBean getConfigBean() { return mConfigBean; }

    /**
        Get an Attribute.  This is a bit tricky, because the target can be an XML attribute,
        an XML string element, or an XML list of elements.
        Performance isn't necessarily very good here because of the brute-force lookup by
        ConfiguredHelper of the amx to xml mapping.
     */
		public final Object
	getAttribute( final String amxName )
	{
        Object result = null;
        
        final ConfiguredHelper.Info info = mConfiguredHelper.getInfo(amxName);
        if ( info == null )
        {
            throw new IllegalArgumentException(amxName);
        }
        
        final String xmlName = info.getXMLName();
        
        //debug( "DelegateToConfigBeanDelegate.getAttribute: amx => xml: " + amxName + " => " + xmlName );
        //debug( "DelegateToConfigBeanDelegate.getAttribute: attrInfo: " + info );
        if ( info instanceof ConfiguredHelper.AttributeInfo )
        {
            result = mConfigBean.rawAttribute( xmlName );
        }
        else
        {
            if ( info.isString() )
            {
                final List<?> leaf = mConfigBean.leafElements(xmlName);
                if ( leaf != null ) {
                    try
                    {
                        result = (String)leaf.get(0);
                    }
                    catch( final Exception e )
                    {
                        // doesn't exist, return null
                    }
                }
            }
            else if ( info.getReturnType() == List.class )
            {
                final List<?> leaf = mConfigBean.leafElements(xmlName);
                if ( leaf != null ) {
                    // verify that it is List<String> -- no other types are supported in this way
                    final List<String> elems = TypeCast.checkList( leaf, String.class );
                    result = CollectionUtil.toArray( elems, String.class);
                }
            }
            else {
                throw new IllegalArgumentException("Unsupported return type: " + info.getReturnType().getName() );
            }
        }
        //debug( "Attribute " + amxName + " has class " + ((result == null) ? "null" : result.getClass()) );
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

        public void unprocessedTransactedEvents(List<UnprocessedChangeEvents> changes) {
            // amx probably does not care that some changes were not processed successfully
            // and will require a restart
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
    {
        try
        {
            return ConfigSupport.class.getDeclaredMethod("defaultPropertyValue", (Class[])null).getGenericReturnType();
        }
        catch( NoSuchMethodException e )
        {
            // not supposed to happen, throw any reasonabl exception
            throw new IllegalArgumentException();
        }
    }    
    
    private static boolean isCollectionCmd( final String s )
    {
        return s != null &&
            (s.equals(OP_ADD) || s.equals(OP_REMOVE) || s.equals(OP_REPLACE) );
    }
    
    
        public String[]
    getAnonymousElementList( final String elementName )
    {
        return (String[])getAttribute( elementName );
    }
    
        public String[]
    modifyAnonymousElementList(
        final String   elementName,
        final String   cmd,
        final String[] values)
    {
        //debug( "modifyAnonymousElementList: " + elementName + ", " + cmd + ", {" + StringUtil.toString(values) + "}" );
        getAnonymousElementList(elementName); // force an error right away if it's a bad name
        
        final String xmlName = mNameMappingHelper.getXMLName(elementName, true);
        try
        {
            final ModifyCollectionApplyer mca = new ModifyCollectionApplyer( mConfigBean, xmlName, cmd, values );
            mca.apply();
            return ListUtil.toStringArray(mca.mResult);
        }
        catch( final TransactionFailure e )
        {
            throw new RuntimeException( "Could not modify element collection " + elementName, e);
        }
    }
    
    /**
        Handle an update to a collection, returning the List<String> that results.
     */
        private List<String>
    handleCollection(
        final WriteableView writeable,
        final ConfigModel.Property prop,
        final String        cmd,
        final List<String>  argValues )
    {
        if ( ! isCollectionCmd(cmd) )
            throw new IllegalArgumentException(""+cmd);
            
        final Object o = writeable.getter(prop, getCollectionGenericType());
        final List<String> masterList = TypeCast.checkList( TypeCast.asList(o), String.class);
        
        //debug( "Existing values: {" + CollectionUtil.toString( masterList ) + "}");
        //debug( "Arg values: {" + CollectionUtil.toString( argValues ) + "}");

        if ( cmd.equals( OP_REPLACE ) )
        {
            masterList.retainAll( argValues );
            for( final String s : argValues )
            {
                if ( ! masterList.contains(s) )
                {
                    masterList.add(s);
                }
            }
            //debug( "Master list after OP_REMOVE: {" + CollectionUtil.toString( masterList ) + "}");
        }
        else if ( cmd.equals( OP_REMOVE ) )
        {
            masterList.removeAll( argValues );
            //debug( "Master list after OP_REMOVE: {" + CollectionUtil.toString( masterList ) + "}");
        }
        else if ( cmd.equals( OP_ADD ) )
        {
            // eliminate duplicates for now unless there is a good reason to allow them
            final List<String> temp = new ArrayList<String>(argValues);
            temp.removeAll(masterList);
            
            masterList.addAll(temp);
            //debug( "Master list after OP_ADD: {" + CollectionUtil.toString( masterList ) + "}");
        }
        else
        {
            throw new IllegalArgumentException(cmd);
        }
        
        //debug( "Existing values list before commit: {" + CollectionUtil.toString( masterList ) + "}");
        return new ArrayList<String>(masterList);
    }
    
    private class Applyer
    {
        final Transaction   mTransaction;
        final ConfigBean    mConfigBean;
        final WriteableView mWriteable;
        
        public Applyer( final ConfigBean cb ) throws TransactionFailure { this(cb, new Transaction()); }
        public Applyer( final ConfigBean cb, final Transaction t)
            throws TransactionFailure
        {
            mConfigBean = cb;
            mTransaction = t;
            
            final ConfigBeanProxy readableView = cb.getProxy( cb.getProxyType() );
            mWriteable = getWriteableView(readableView, cb );
        }
        
        protected void makeChanges() 
            throws TransactionFailure  {}
        
        final void apply()
            throws TransactionFailure
        {
            try
            {
                joinTransaction(mTransaction, mWriteable);
                
                makeChanges();
                
                commit(mTransaction);
            }
            finally
            {
                mConfigBean.getLock().unlock();
            }
        }
    }
    
    private final class ModifyCollectionApplyer extends Applyer
    {
        private volatile List<String> mResult;
        private final String   mElementName;
        private final String   mCmd;
        private final String[] mValues;
        
        public ModifyCollectionApplyer(
            final ConfigBean    cb,
            final String elementName,
            final String cmd,
            final String[] values )
            throws TransactionFailure
        {
            super( cb );
            mElementName = elementName;
            mCmd = cmd;
            mValues = values;
            mResult = null;
        }
        
        protected void makeChanges()
            throws TransactionFailure
        {
            final ConfigModel.Property prop = mNameMappingHelper.getConfigModel_Property(mElementName);
            mResult = handleCollection( mWriteable, prop, mCmd, ListUtil.asStringList(mValues));
        }
    }
    
    private final class MakeChangesApplyer extends Applyer
    {
        private final Map<String,Object> mChanges;
        
        public MakeChangesApplyer(
            final ConfigBean cb,
            final Map<String,Object> changes)
            throws TransactionFailure

        {
            super(cb);
            mChanges = changes;
        }
                
        protected void makeChanges()
            throws TransactionFailure
        {
            for ( final String xmlName : mChanges.keySet() )
            {
                final Object value = mChanges.get(xmlName);
                final ConfigModel.Property prop = mNameMappingHelper.getConfigModel_Property(xmlName);

                if ( prop.isCollection() )
                {
                    final List<String> results = handleCollection( mWriteable, prop, OP_REPLACE, ListUtil.asStringList(value) );
                }
                else if ( value == null || (value instanceof String) )
                {
                    mWriteable.setter( prop, value, String.class);
                }
                else
                {
                    throw new TransactionFailure( "Illegal data type for attribute " + xmlName + ": " + value.getClass().getName() );
                }
            }
        }
    }
    
    private void apply(
        final ConfigBean cb,
        final Map<String,Object> changes )
        throws TransactionFailure
    {
        final MakeChangesApplyer mca = new MakeChangesApplyer( mConfigBean, changes );
        mca.apply();
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
            //debug( "DelegateToConfigBeanDelegate.setAttributes(): " + attrsIn.size() + " attributes: {" +
           //     CollectionUtil.toString(amxAttrs.keySet()) + "} mapped to xml names {" + CollectionUtil.toString(xmlAttrs.keySet()) + "}");
            
            final MyTransactionListener  myListener = new MyTransactionListener( mConfigBean );
            final Transactions transactions = mConfigBean.getHabitat().getComponent(Transactions.class);
            transactions.addTransactionsListener(myListener);
                
            // results should contain only those that succeeded which will be all or none
            // depending on whether the transaction worked or not
            try
            {
                final MakeChangesApplyer mca = new MakeChangesApplyer( mConfigBean, xmlAttrs );
                mca.apply();

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
                transactions.waitForDrain();

                transactions.removeTransactionsListener(myListener);
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











