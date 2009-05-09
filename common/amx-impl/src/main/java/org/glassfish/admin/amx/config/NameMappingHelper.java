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
import com.sun.appserv.management.util.misc.GSetUtil;
import com.sun.appserv.management.util.misc.ListUtil;
import com.sun.appserv.management.util.misc.CollectionUtil;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.glassfish.admin.amx.util.AMXConfigInfoResolver;
import org.glassfish.api.amx.AMXConfigInfo;

import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigBeanProxy;



/**
    Encapsulate the logic for mapping AMX attribute names to XML attribute names, and a few
    related support items.
    <p>
    Code in here should be reconciled with ConfiguredHelper; there is overlap.
 */
final class NameMappingHelper {
    private static void debug( final String s ) { System.out.println(s); }
    
    private final String      mJ2EEType;
    private final ConfigBean  mConfigBean;
    
    public NameMappingHelper( final ConfigBean configBean ) {
        mJ2EEType = getAMXConfigInfoResolver(configBean).j2eeType();
        mConfigBean  = configBean;
    }
    
		void
	initNameMapping( final String[] amxAttrNames )
	{
        NameMapping mapping = NameMappingRegistry.getInstance(mJ2EEType);
        if ( mapping == null )
        {
            final Set<String> xmlAttrNames = mConfigBean.model.getAttributeNames();
            final Set<String> xmlLeafNames = mConfigBean.model.getElementNames();
            final Set<String> xmlNames = new HashSet<String>( xmlAttrNames );
            xmlNames.addAll( xmlLeafNames );
            
            mapping = new NameMapping(mJ2EEType);
            
            final Set<String> amxNames = GSetUtil.newStringSet(amxAttrNames);
            
            // special case: the name, look for a hint in the annotation
            if ( amxNames.contains(AMXAttributes.ATTR_NAME) )
            {
                final ConfiguredHelper helper = ConfiguredHelperRegistry.getInstance(mConfigBean.getProxyType());
                
                final String hint = helper.getNameHint();
                if ( hint != null && hint.length() != 0 )
                {
                    //debug( "smartNameFind: mapped " + amxName + " to " + hint + " for " + info.amxInterface().getName() ); 
                    amxNames.remove(AMXAttributes.ATTR_NAME);
                    mapping.pairNames(AMXAttributes.ATTR_NAME, hint);
                }
            }
            
            // now map all remaining attributes from their AMX name to the xml name
            for( final String amxAttrName : amxNames )
            {
                final String xmlName = mapping.pairNames( amxAttrName, xmlNames );
                if ( xmlName != null )
                {
                    // matched, so reduce the search set size
                    xmlNames.remove(xmlName);
                }
                else
                {
                    //debug( "Could not find xmlName for: " + amxAttrName );
                }
            }
            NameMappingRegistry.addInstance( mapping );
        }
    }
                        
    public ConfigBean getConfigBean()   { return mConfigBean; }
    public NameMapping getNameMapping() { return NameMappingRegistry.getInstance(mJ2EEType); }

        public static AMXConfigInfoResolver
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
    
        public AMXConfigInfoResolver
    getAMXConfigInfoResolver()
    {
        return getAMXConfigInfoResolver(mConfigBean);
    }
       
    /**
        Belongs in HK2 itself (Dom.java), but here until versioning can be worked out.
     */
        protected ConfigModel.Property
    getConfigModel_Property( final String xmlName ) {
        return getConfigModel_Property( mConfigBean, xmlName );
    }
    
        public static ConfigModel.Property
    getConfigModel_Property( final ConfigBean cb, final String xmlName ) {
        final ConfigModel.Property cmp = cb.model.findIgnoreCase(xmlName);
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
        Get an AttrInfo based on the AMX attribute name.
     */
        public AttrInfo
    getAttrInfo_AMX( final String amxName )
    {
        final String xmlName = getXMLName(amxName, true);

        final boolean isLeaf       = isLeaf(xmlName);
        final boolean isCollection = isCollection(xmlName);

        return new AttrInfo( amxName, xmlName, isLeaf, isCollection );
    }

    
        public final String
    getXMLName( final String amxName )
    {
        return getXMLName( amxName, false );
    }
    
    /**
        Get the XML attribute name corresponding to the AMX attribute name.
     */
        public final String
    getXMLName( final String amxName, final boolean friendlyMatching )
    {
        final String xmlName = getNameMapping().getXMLName( amxName, friendlyMatching);
        return xmlName;
    }
    
    /**
        Be 'friendly' for a few types like Boolean, Integer, Long.
        Leave String[] and null alone.
     */
        public static Object
    asStringType( final Object o )
    {
        Object result = o;
        
        if ( o != null )
        {
            if ( o == null || (o instanceof String) )
            {
                result = o;
            }
            else if ( (o instanceof Boolean) || (o instanceof Integer) || (o instanceof Long) )
            {
                result = "" + o;
            }
            else if ( ! (o instanceof String[]) )
            {
                throw new IllegalArgumentException( "Illegal value: " + o + " of class " + o.getClass().getName() );
            }
        }
        
        return result;
    }
    
    /**
        Transform a Map keyed by AMX attribute names into one keyed by XML attribute names.
     */
        public Map<String,Object>
    mapNamesAndValues(
        final Map<String,Object> amxAttrs,
        final Map<String,Object> noMatch )
    {
        final Map<String,Object> xmlAttrs = new HashMap<String,Object>();
        
        for( final String amxAttrName : amxAttrs.keySet() )
        {
            final Object valueIn = amxAttrs.get(amxAttrName);
            final String xmlName = getXMLName(amxAttrName);
            if ( xmlName != null )
            {
                final Object value   = asStringType( valueIn );
                if ( value != valueIn )
                {
                    //debug( "Attribute " + amxAttrName + " auto converted from " + valueIn.getClass().getName() + " to " + value.getClass().getName() );
                }
                
                // We accept only Strings, String[] or null
                if ( valueIn == null || (value instanceof String))
                {
                    xmlAttrs.put( xmlName, (String)value);
                }
                else if ( isCollection(xmlName) )
                {
                    if ( (valueIn instanceof String[]) || (valueIn instanceof List) )
                    {
                        xmlAttrs.put( xmlName, ListUtil.asStringList(valueIn) );
                    }
                    else
                    {
                        noMatch.put( amxAttrName, valueIn );
                    }
                }
                else
                {
                    noMatch.put( amxAttrName, valueIn );
                }
               // debug( "Attribute " + amxAttrName + "<=>" + xmlName + " is of class " + ((value == null) ? null : value.getClass().getName()) );
            }
            else
            {
                debug( "WARNING: setAttributes(): no xmlName match found for AMX attribute: " + amxAttrName );
                noMatch.put( amxAttrName, valueIn );
            }
        }
        
        return xmlAttrs;
    }

}








