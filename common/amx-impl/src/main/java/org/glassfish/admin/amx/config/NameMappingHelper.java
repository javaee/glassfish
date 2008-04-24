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
 */
final class NameMappingHelper {
    private static void debug( final String s ) { System.out.println(s); }
    
    private final NameMapping mNameMapping;
    private final ConfigBean  mConfigBean;
    
    public NameMappingHelper( final ConfigBean configBean ) {
        mNameMapping = NameMapping.getInstance( getAMXConfigInfoResolver(configBean).j2eeType() );
        mConfigBean  = configBean;
    }
          
    public ConfigBean getConfigBean()   { return mConfigBean; }
    public NameMapping getNameMapping() { return mNameMapping; }

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
        Get an AttrInfo based on the AMX attribute name.
     */
        public AttrInfo
    getAttrInfo_AMX( final String amxName )
    {
        final String xmlName = getXMLName(amxName);

        final boolean isLeaf       = isLeaf(xmlName);
        final boolean isCollection = isCollection(xmlName);

        return new AttrInfo( amxName, getXMLName( amxName), isLeaf, isCollection );
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
        public final String
    getXMLName( final String amxName )
    {
        String xmlName = mNameMapping.getXMLName( amxName );
        if ( xmlName == null )
        {
            xmlName = smartAttrNameFind( amxName );
            if ( xmlName == null )
            {
                final Set<String> xmlNames = mConfigBean.getAttributeNames();
                //debug( "matching " + amxName + " against xml Attribute names: {" + CollectionUtil.toString( xmlNames ) + "}");
                
                xmlName = mNameMapping.matchAMXName( amxName, xmlNames );
                if ( xmlName == null )
                {
                    //final Set<String> leafNames = mConfigBean.getLeafElementNames();
                    final Set<String> leafNames = GSetUtil.newStringSet( ConfigSupport.getElementsNames(mConfigBean) );
                    //debug( "Matching " + amxName + " against xml leaf element names: {" + CollectionUtil.toString(leafNames) + "}" );
                    xmlName = mNameMapping.matchAMXName( amxName, leafNames );
                }
                //debug( "Matched: " + amxName + " => " + xmlName );
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
                final Object value   = autoStringify( valueIn );
                if ( value != valueIn )
                {
                    debug( "Attribute " + amxAttrName + " auto converted from " +
                        valueIn.getClass().getName() + " to " + value.getClass().getName() );
                }
                
                // We accept only Strings or null values
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
                debug( "Attribute " + amxAttrName + "<=>" + xmlName +
                    " is of class " + ((value == null) ? null : value.getClass().getName()) );
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








