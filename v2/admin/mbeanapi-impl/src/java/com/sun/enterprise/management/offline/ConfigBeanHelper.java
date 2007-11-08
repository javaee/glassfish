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
 

package com.sun.enterprise.management.offline;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import java.io.IOException;


import javax.management.ObjectName;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;

import com.sun.appserv.management.base.AMXDebug;

import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.StringUtil;
import com.sun.appserv.management.util.misc.ListUtil;

import com.sun.enterprise.management.support.oldconfig.OldProps;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigException;


import com.sun.enterprise.config.util.ConfigXPathHelper;

//import com.sun.enterprise.config.serverbeans.*;

abstract class ConfigBeanHelper

{
    private final ConfigBean    mConfigBean;
    private final String        mXPath;
    private final ConfigContext mConfigContext;
    
    private final String        mType;
    private final String        mName;  // may be null!

    static private final String NAME_ATTR   = "Name";
    static private final String VALUE_ATTR   = "Value";
    static private final String DESCRIPTION_ATTR   = "Description";
    
    static protected final String[]   EMPTY_STRING_ARRAY  = new String[0];
    
    ConfigBeanHelper(
        final ConfigContext configContext,
        final ConfigBean    configBean )
    {
        if ( configBean == null )
        {
            throw new IllegalArgumentException( "null configBean" );
        }
        if ( configContext == null )
        {
            throw new IllegalArgumentException( "null configContext" );
        }
        
        // why does configBean.getConfigContext() return null?
        mConfigContext  = configContext;
        
        mConfigBean     = configBean;
        mXPath          = mConfigBean.getXPath();
        assert( mXPath != null );
        
        mType   = _getType( mXPath );
        mName   = _getName();
        
    }
    
       protected void
	debug( Object o )
	{
	    sdebug( o );
	    
	    final String    xpath   = "" + mXPath;
	    AMXDebug.getInstance().getOutput( "ConfigBeanHelper" ).println( xpath + ": " + o );
	}
	
	    protected void
	sdebug( Object o )
	{
	    System.out.println( "" + o );
	}
	
    
        public ConfigBean
    getConfigBean()
    {
        return mConfigBean;
    }
    
        public ConfigContext
    getConfigContext()
    {
        return mConfigContext;
    }

    	public Object
    getAttribute( final String attrName )
    	throws AttributeNotFoundException
    {
        Object  result  = null;
        
        if ( DESCRIPTION_ATTR.equals( attrName ) )
        {
            result  = getDescription();
        }
        else
        {
            try
            {
        	    result   = mConfigBean.getAttributeValue( attrName );
    	    }
    	    catch( RuntimeException e )
    	    {
        	    debug( "ATTR FAILED: " + attrName );
        	    throw e;
    	    }
	    }
	    return result;
    }
    
    	public String
    getXPath()
    {
    	return mXPath;
    }
    
    	public String
    getParentXPath()
    {
    	return ConfigXPathHelper.getParentXPath( getXPath() );
    }
    
    /**
        @return type, always non-null
     */
    	public String
    getType()
    {
    	return mType;
    }
    
    	static String
    _getType( final String xPath )
    {
    	return ConfigXPathHelper.getLastNodeName( xPath );
    }
    
    /**
        @return name, possibly null if no name
     */
        public String
    getName()
    {
        return mName;
    }
    
    /**
        @return String[2], where [0] is the type and [1] is the name
     */
        public String[]
    getTypeAndName()
        throws ConfigException
    {
        return new String[] { getType(), getName() };
    }
    
    	private String
    _getName()
    {
        String  name    = null;
        
        final Set<String>  attrNames   = GSetUtil.newStringSet( getAttributeNames() );
                    
        for( final String nameKey : OldProps.getPossibleNameKeys() )
        {
            final String    camelized   = mConfigBean.camelize( nameKey );
            
            if ( attrNames.contains( camelized ) )
            {
                try
                {
                    name    = (String)getAttribute( camelized );
                    if ( name != null )
                    {
                        break;
                    }
                }
                catch( AttributeNotFoundException e )
                {
                    throw new RuntimeException( e );
                }
            }
        }
        
        return name;
    }
    
    
        public List<String[]>
    getAllObjectNameProps( final Set<String> ignoreTypes )
        throws ConfigException
    {
        final List<String[]>   props   = new ArrayList<String[]>();
        
        //debug( "\n--------------------------------------" );
       // debug( xPath );
        String[] pair    = getTypeAndName();
        props.add( pair );
        
        String  curXPath = ConfigXPathHelper.getParentXPath( getXPath() );
        String  lastXPath    = null;
        while ( ! curXPath.equals( lastXPath ) )
        {
            final ConfigBeanHelper  helper  =
                ConfigBeanHelperFactory.getInstance( getConfigContext() ).getHelper( curXPath );
            
            pair    = helper.getTypeAndName();
            
            if ( ! ignoreTypes.contains( pair[ 0 ] ) )
            {
                props.add( pair );
            }
            
            lastXPath   = curXPath;
            curXPath    = helper.getParentXPath();
        }
        
        return props;
    }
    

    	public void
    setAttribute( final Attribute attr )
    	throws AttributeNotFoundException, InvalidAttributeValueException
    {
        final Object    value   = attr.getValue();
        
    	setAttribute( attr.getName(), value );
    }
    
    	public void
    setAttribute( final String name, final Object value )
    	throws AttributeNotFoundException, InvalidAttributeValueException
    {
        if ( DESCRIPTION_ATTR.equals( name ) )
        {
            setDescription( (String)value );
        }
        else
        {
            // Config API wants strings (only)
            final String valueString   = (value == null) ? null : ("" + value);
    	    mConfigBean.setAttributeValue( name, valueString );
    	}
    }
    
    
        protected boolean
    hasValue( final String valueName )
    {
        boolean hasValue  = false;
        
        try
        {
            final Object    value   = mConfigBean.getValue( valueName );
            // value may be null, but it still indicates that the value is possible
            hasValue  = true;
            //sdebug( "HAS VALUE " + valueName + "=" + value );
        }
        catch( Exception e )
        {
        }
        
        return hasValue;
    }


        private boolean
    hasDescription()
    {
        return hasValue( DESCRIPTION_ATTR );
    }

        public final String[]
    getAttributeNames()
    {
        return GSetUtil.toStringArray( _getAttributeNames() );
    }
    
        protected Set<String>
    _getAttributeNames()
    {
        final Set<String>   attrNames =
            GSetUtil.newSet( mConfigBean.getAttributeNames() );
        
        // it does this sometimes!
        assert( ! attrNames.contains( null ) );
        attrNames.remove( null );
            
        if ( hasDescription() )
        {
            attrNames.add( DESCRIPTION_ATTR );
        }
        
        return attrNames;
    }

    /**
        Subclass may override this for special cases that are non-String.
     */
        protected Class
    getAttributeClass( final String attrName )
    {
        return String.class;
    }
    
    /**
        Subclass may override this, or alternatly, just specify
        the Class of the Attribute by overriding {@link #getAttributeClass}.
     */
        protected MBeanAttributeInfo
    getMBeanAttributeInfo( final String attrName )
    {
        final String    description = "";
        final boolean   isReadable  = true;
        final boolean   isWriteable  = true;
        final boolean   isIs  = false;
        
        assert( attrName != null );
        final MBeanAttributeInfo    info = new MBeanAttributeInfo(
            attrName,
            getAttributeClass( attrName ).getName(),
            description,
            isReadable,
            isWriteable,
            isIs );
        
        return info;
    }

    	public MBeanInfo
    getMBeanInfo()
    {
        final List<String>  attrNames   = ListUtil.newListFromArray( getAttributeNames() );
        
        final MBeanOperationInfo[]  operationInfos  = new MBeanOperationInfo[0];
        final MBeanAttributeInfo[]  attributeInfos  = new MBeanAttributeInfo[ attrNames.size() ];
        
        int i = 0;
        for( final String name : attrNames )
        {
            assert( name != null );
            attributeInfos[ i ] = getMBeanAttributeInfo( name );
            assert attributeInfos[ i ].getName() != null;
            
            ++i;
        }
                    
        final MBeanInfo	info	=
            new MBeanInfo( this.getClass().getName(),
                "exposes Attributes from ConfigBean",
                attributeInfos,
                null,
                operationInfos,
                null );
        
        for( final MBeanAttributeInfo xxx  : info.getAttributes() )
        {
            assert( xxx.getName() != null );
        }

		return info;
    }
    
        public List<ConfigBeanHelper>
    getAllChildren()
    {
        final List<ConfigBeanHelper>  children    = new ArrayList<ConfigBeanHelper>();
        
        final ConfigBean[]  configBeans    = mConfigBean.getAllChildBeans();
        if ( configBeans != null )
        {
            for( final ConfigBean configBean : configBeans )
            {
                if ( configBean != null )
                {
                    final ConfigBeanHelper  helper =
                        ConfigBeanHelperFactory.getInstance( getConfigContext() ).getHelper( configBean );
                    
                    children.add( helper );
                }
            }
        }
        
        return children; 
    }
    
        public List<ConfigBeanHelper>
    getAllChildrenOfType( final String desiredType )
    {
        final List<ConfigBeanHelper>  children  = getAllChildren();
        final List<ConfigBeanHelper>  propertyChildren = new ArrayList<ConfigBeanHelper>();
        
        for( final ConfigBeanHelper helper : children )
        {
            if ( desiredType.equals( helper.getType() ) )
            {
                propertyChildren.add( helper );
            }
        }
        
        return propertyChildren; 
    }
    
    

        public Map<String,ConfigBeanHelper>
    getSpecialChildMap( final String type )
    {
        if ( ! ("element-property".equals( type ) ||
            "system-property".equals( type )) ||
            "jvm-option".equals( type ) )
        {
            throw new IllegalArgumentException( type );
        }

        final List<ConfigBeanHelper> children = getAllChildrenOfType( type );
        final Map<String,ConfigBeanHelper>  m   = new HashMap<String,ConfigBeanHelper>();
        
        for( final ConfigBeanHelper helper : children )
        {
            try
            {
                final String name   = (String)helper.getAttribute( NAME_ATTR );
                
                m.put( name, helper );
            }
            catch( Exception e )
            {
                throw new RuntimeException(
                    "FAILURE getting Name Attribute for property element", e );
            }
        }
        
        return m;
    }
    
    
    /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public AttributeList
    getProperties()
    {
        final Map<String,ConfigBeanHelper>  children =
            getSpecialChildMap( "element-property" );
        
        final AttributeList attrs   = new AttributeList();
        
        for( final String name : children.keySet() )
        {
            final ConfigBeanHelper  helper  = children.get( name );
            
            final String value  = getValueAttributeValue( helper );
            
            attrs.add( new Attribute( name, value ) );
        }
        
        return attrs;
    }
    
      
    /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public AttributeList
    getSystemProperties()
    {
        final Map<String,ConfigBeanHelper>  children =
            getSpecialChildMap( "system-property" );
        
        final AttributeList attrs   = new AttributeList();
        for( final String name : children.keySet() )
        {
            final ConfigBeanHelper  helper  = children.get( name );
            
            final String value  = getValueAttributeValue( helper );
            
            attrs.add( new Attribute( name, value ) );
        }
        
        return attrs;
    }
    
    /**
     */
        protected static String
    getValueAttributeValue( final ConfigBeanHelper  helper )
    {
        try
        {
            final String value  = (String)helper.getAttribute( VALUE_ATTR );
            return value;
        }
        catch( Exception e )
        {
            throw new RuntimeException(
                "FAILURE getting Value Attribute for property element ", e );
        }
    }
    
     /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public String
    getPropertyValue( final String propertyName )
    {
        final Map<String,ConfigBeanHelper>  children =
            getSpecialChildMap( "element-property" );
        if ( ! children.containsKey( propertyName ) )
        {
            final String msg    = "No such property: " +
                StringUtil.quote( propertyName );
                
            throw new IllegalArgumentException( msg );
        }
        
        return getValueAttributeValue( children.get( propertyName ) );
    }
    
     /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public String
    getSystemPropertyValue( final String propertyName )
    {
        final Map<String,ConfigBeanHelper>  children =
            getSpecialChildMap( "system-property" );
        if ( ! children.containsKey( propertyName ) )
        {
            throw new IllegalArgumentException( propertyName );
        }
        
        return getValueAttributeValue( children.get( propertyName ) );
    }
    
    
        private static void
    setValueAttributeValue(
        final ConfigBeanHelper  helper,
        final String            value )
    {
        try
        {
            helper.setAttribute( VALUE_ATTR, value );
        }
        catch( Exception e )
        {
            throw new RuntimeException(
                "FAILURE setting Value Attribute for property element ", e );
        }
    }
    
     /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public void
    setProperty( final Attribute attr )
    {
        final Map<String,ConfigBeanHelper>  children =
            getSpecialChildMap( "element-property" );
        
        // this won't work for creating a new property!
        final ConfigBeanHelper helper = children.get( attr.getName() );
        if ( helper == null )
        {
            throw new IllegalArgumentException( attr.getName() );
        }
        
        setValueAttributeValue( helper, (String)attr.getValue() );
    }
    
     /**
        @see com.sun.enterprise.management.config.OldPropertiesImpl
     */
        public void
    setSystemProperty( final Attribute attr )
    {
        final Map<String,ConfigBeanHelper>  children =
            getSpecialChildMap( "system-property" );
        
        // this won't work for creating a new property!
        final ConfigBeanHelper helper = children.get( attr.getName() );
        if ( helper == null )
        {
            throw new IllegalArgumentException( attr.getName() );
        }
        
        setValueAttributeValue( helper, (String)attr.getValue() );
    }
    
        public String
    getDescription()
    {
        String  result  = (String)getValue( DESCRIPTION_ATTR );
        
        return result;
    }
    
        public void
    setDescription( final String description )
    {
        mConfigBean.setValue( DESCRIPTION_ATTR, description );
    }
    
    /**
        When the ConfigBean is called for an array value that
        can potentially exist, but doesn't, it tries to access an
        array illegally.  Not much we can do except return null.
        <p>
        Here's the stack trace:
        
        java.lang.IndexOutOfBoundsException: Index: 0, Size: 0
         at java.util.ArrayList.RangeCheck(ArrayList.java:547)
         at java.util.ArrayList.get(ArrayList.java:322)
         at org.netbeans.modules.schema2beans.BeanProp.getValue(BeanProp.java:474)
         at org.netbeans.modules.schema2beans.BaseBean.getValue(BaseBean.java:353)
         at com.sun.enterprise.config.ConfigBean.getValue(ConfigBean.java:383)
     */
        protected Object
    getValue( final String valueName )
    {
        Object  value  = null;
        
        try
        {
            value = mConfigBean.getValue( valueName );
        }
        catch( Exception e )
        {
            debug( "ConfigBeanHelper.getValue: Exception accessing value: " + valueName );
            //e.printStackTrace();
        }
        
        return value;
    }
    
        public abstract Object
    handleInvoke(
    	String 		operationName,
    	Object[]	args,
    	String[]	types );
    	
    	 public void
    unsupportedOperation(
    	String 		operationName,
    	Object[]	args,
    	String[]	types )
    {
        throw new IllegalArgumentException(
            "invoke() unknown operation " + operationName + "()" );
    }
}

















