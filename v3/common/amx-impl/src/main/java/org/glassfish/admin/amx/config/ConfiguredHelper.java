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

import com.sun.appserv.management.util.jmx.JMXUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.lang.reflect.Method;

import org.glassfish.api.amx.AMXConfigInfo;
import org.glassfish.admin.amx.util.AMXConfigInfoResolver;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DuckTyped;

import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.AMXGenericConfig;


/**
    Reads the annotations on an @Configured and sorts them out into useful groups
    for use in mapping to AMX constructs.
 */
final class ConfiguredHelper
{
    private static void debug( final String s ) { System.out.println(s); }
    
    private final  Class<? extends ConfigBeanProxy>  mIntf;
    private final Configured    mConfigured;
    private final AMXConfigInfo mAMXConfigInfo;
    
    /** Record {"Foo","foo"} / {"FooBar", "foo-bar"} relationship */
    private static class Info
    {
        private final String name;
        private final String xmlName;
        public Info( final String name, final String xmlName)
        {
            this.name    = name;
            this.xmlName = xmlName;
        }
        public String getXMLName() { return xmlName; }
        public String getName()    { return name; }
    }
    
    private static final class AttributeInfo extends Info
    {
        private final Attribute attr;
        public AttributeInfo( final String getterName, final Attribute a )
        {
            super( getterName, a.value().length() != 0 ? a.value() : Dom.convertName(getterName));
            attr = a;
        }
        public Attribute getAttribute() { return attr; }
    }
    
    private static final class ElementInfo extends Info
    {
        private final Element elem;
        public ElementInfo( final String getterName, final Element e )
        {
            super( getterName, e.value().length() != 0 ? e.value() : Dom.convertName(getterName) );
            elem = e;
        }
        public Element getElement() { return elem; }
    }
    
    
    /** Keyed by xml name */
    private final Map<String,AttributeInfo> mAttributes = new HashMap<String,AttributeInfo>();
    
    /** Keyed by xml name  */
    private final Map<String,ElementInfo>   mElements   = new HashMap<String,ElementInfo>();
    
    /* Maps method name to method */
    private final Map<String,Method>    mDuckTypedMethods  = new HashMap<String,Method>();
    
    /** the Attribute or element name used for the name */
    private final String mNameHint;
       
    public ConfiguredHelper( final Class<? extends ConfigBeanProxy> intf )
    {
        mIntf = intf;
        
        mConfigured = intf.getAnnotation( Configured.class );
        if ( mConfigured == null )
        {
            throw new IllegalArgumentException( "ConfigBeanProxy is not an @Configured" );
        }
        
        mAMXConfigInfo = intf.getAnnotation( AMXConfigInfo.class );
        
        findStuff();
        
        mNameHint = findNameHint();
    }
    
    
    public Class<? extends ConfigBeanProxy> getIntf() { return mIntf; }
    
    /** 
        Match the name to an XML name, returning null if no match. Not intended for
        high-performance use; mapping should be maintained elsewhere.
     */
        public String
    findXMLName( final String anyName )
    {
        // lowercase, no dashes
        final String canonical = anyName.toLowerCase().replace("-","");
        
        for( final AttributeInfo info : mAttributes.values() )
        {
            // have to check regular name and xml name for a match;
            // they could be wildly different consider<br>
            // <code>@Attribute(name="hello-there")  String getFooBar()</code>
            if ( info.getName().equalsIgnoreCase(canonical) )
            {
                return info.getXMLName();
            }
            
            final String temp = info.getXMLName().replace( "-", "");
            if ( canonical.equalsIgnoreCase(temp) )
            {
                return info.getXMLName();
            }
        }
        
        for( final ElementInfo info : mElements.values() )
        {
            if ( info.getName().equalsIgnoreCase(canonical) )
            {
                return info.getXMLName();
            }
            
            final String temp = info.getXMLName().replace( "-", "");
            if ( canonical.equalsIgnoreCase(temp) )
            {
                return info.getXMLName();
            }
        }
        
        return null;
    }


    /**
        Get the default values, keyed by the XML element name.  The interface might be
        for this MBean or one of its children.
     */
        final Map<String,String>
    getDefaultValues()
    {
        final Map<String,String> result = new HashMap<String,String>();
        
        for( final AttributeInfo info : mAttributes.values() ) 
        {
            // don't put null values into defaults (see @Attribute annotation)
            final String value = info.getAttribute().defaultValue();
            final boolean isEmptyDefault = value.equals( "\u0000" );
            //cdebug( "Method " + m + " has default value of " + (emptyDefault ? "\\u0000" : value) );
            if ( ! isEmptyDefault )
            {
                result.put( info.getXMLName(), "" + value );
            }
        }
        
        return result;
    }

    
    public String getNameHint() { return mNameHint; }
    
    /** return the @AMXConfigInfo if present */
    public AMXConfigInfo  getAMXConfigInfo()
    {
        return mAMXConfigInfo;
    }
    
    /** Get the AMX interface (if available and it can be loaded) */
    public Class<? extends AMXConfig> getAMXInterface()
    {
        Class<? extends AMXConfig>  amxIntf = null;
        
        final AMXConfigInfo amxConfigInfo = getAMXConfigInfo();
        if ( amxConfigInfo != null )
        {
            final AMXConfigInfoResolver resolver = new AMXConfigInfoResolver(amxConfigInfo);
            
            try
            {
                amxIntf = resolver.amxInterface();
            }
            catch( final Exception e )
            {
                e.printStackTrace();
            }
        }
        
        // if there is no interface available, use AMXGenericConfig
        return amxIntf == null ? AMXGenericConfig.class : amxIntf;
    }
    
    /**
        Return the implied field names that AMX should use.
        Attribute capitalization will match the @Configured getter names.  This could differ
        from that of the AMX interface eg "HTTPListener" vs "HttpListener" vs "httplistener".
    */
    public Set<String> getImpliedAMXNames()
    {
        return getNames();
    }
    
    /**
        Get all XML names.  Note that due to annotations an XML name for "FooBar" could
        be "hello-there" instead of the default "foo-bar".
     */
    public Set<String> getXMLNames()
    {
        final Set<String> names = new HashSet<String>(mAttributes.keySet());
        names.addAll( mElements.keySet() );        
        return names;
    }
    
    /**
        Get all getter names eg "Foo", "Bar", "FooBar" corresponding to XML names
        "foo", "bar", "foo-bar".
     */
    public Set<String> getNames()
    {
        final Set<String> names = new HashSet<String>();
        
        for( final AttributeInfo info : mAttributes.values() )
        {
            names.add( info.getName() );
        }
        
        for( final ElementInfo info : mElements.values() )
        {
            names.add( info.getName() );
        }
              
        return names;
    }
    
    /*
    public Map<String,Attribute>  getAttributes()
    {
        return mAttributes;
    }
    
    public Map<String,Element>  getElements()
    {
        return mElements;
    }
    */
    
    public Map<String,Method>  getDuckTypedMethods()
    {
        return mDuckTypedMethods;
    }
  
    private void findStuff()
    {
        final Method[] methods = mIntf.getMethods();
        
        // find @Attribute, @Element and @DuckTyped stuff
        for( final Method m : methods )
        {
            Element   e = null;
            DuckTyped dt = null;
            final Attribute a = m.getAnnotation( Attribute.class );
            
            if ( a != null )
            {
                if ( ! JMXUtil.isIsOrGetter(m) ) { continue; }
                
                final AttributeInfo info = new AttributeInfo( JMXUtil.getAttributeName(m), a );
                mAttributes.put( info.getXMLName(), info );
            }
            else if ( (e = m.getAnnotation(Element.class)) != null )
            {
                if ( ! JMXUtil.isIsOrGetter(m) ) { continue; }
                
                final ElementInfo info = new ElementInfo( JMXUtil.getAttributeName(m), e );
                mElements.put(  info.getXMLName(), info );
            }
            else if ( (dt = m.getAnnotation(DuckTyped.class)) != null )
            {
                mDuckTypedMethods.put( m.getName(), m );
            }
            else
            {
                // ignore
            }
        }
    }
    
    private String findNameHint()
    {
        if ( mAMXConfigInfo != null && mAMXConfigInfo.nameHint().length() != 0 )
        {
            return mAMXConfigInfo.nameHint();
        }
        
        for( final AttributeInfo info : mAttributes.values() )
        {
            if ( info.getAttribute().key() )
            {
                return info.getName();
            }
        }
        
        for( final ElementInfo info : mElements.values() )
        {
            if ( info.getElement().key() )
            {
                return info.getName();
            }
        }
        
        final String NAME = "name";
        if ( mAttributes.get(NAME) == null && mElements.get(NAME) == null )
        {
            throw new IllegalArgumentException( "No key value found and no Attribute or Element named 'name'" );
        }
        
        return NAME;
    }
}

































