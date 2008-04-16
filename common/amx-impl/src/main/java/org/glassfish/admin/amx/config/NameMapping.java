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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
    Maintains a cache from AMX Attribute names to XML attribute names.
    Because Attributes of the same AMX name could map to different XML names
    (eg Name => name or Name => id), a distinct mapping must be maintained for every j2eeType.
 */
final class NameMapping
{
    /**
        One Map for every j2eeType.
     */
    private static final ConcurrentMap<String,NameMapping>  INSTANCES = new ConcurrentHashMap<String,NameMapping>();
    
    private final String mJ2EEType;
    private final ConcurrentMap<String,String>  mAMXToXML = new ConcurrentHashMap<String,String>();
    private final ConcurrentMap<String,String>  mXMLToAMX = new ConcurrentHashMap<String,String>();
    
    private NameMapping( final String j2eeType )
    {
        mJ2EEType = j2eeType;
    }
    
    private static void debug( final String s ) { System.out.println(s); }
    
        public static NameMapping
    getInstance( final String j2eeType )
    {
        NameMapping inst = INSTANCES.get(j2eeType);
        if ( inst == null )
        {
            final NameMapping newInst = new NameMapping(j2eeType) ;
            final NameMapping existing = INSTANCES.putIfAbsent( j2eeType, newInst);
            inst = existing != null ? existing : newInst;
        }
        
        return inst;
    }
    
    
    /**
        Given an AMX name, get the XML name.
     */
        public String
    getXMLName( final String amxName )
    {
        return mAMXToXML.get( amxName );
    }
    
    /**
        Given an XML name, get the AMX name.
     */
        public String
    getAMXName( final String xmlName )
    {
        return mXMLToAMX.get( xmlName );
    }

        public void
    pairNames( final String amxName, final String xmlName )
    {
        mAMXToXML.put( amxName, xmlName );
        mXMLToAMX.put( xmlName, amxName );
    }
    
    /** 
        Match the AMX attribute name to an XML attribute name, adding it to the cache
        as a side-effect.
     */
        public String
    matchAMXName( final String amxName, final Set<String> xmlCandidates )
    {
        // the only AMX Attribute that we pass through is ATTR_NAME, which maps to
        // a real attribute in config. So don't waste time on any AMX attributes
        // which don't exist in config.
        if ( AMXAttributes.AMX_ATTR_NAMES.contains(amxName) &&
            ! AMXAttributes.ATTR_NAME.equalsIgnoreCase(amxName) )
        {
            return null;
        }
        
        final String amxCanonical = amxName.toLowerCase();
        String xmlName = null;
        
        for (final String xmlCandidate : xmlCandidates )
        {
            final String temp = xmlCandidate.replace( "-", "");
            if ( temp.equals( amxCanonical ) )
            {
                xmlName = xmlCandidate;
                break;
            }
        }
        
        if ( xmlName != null )
        {
            pairNames( amxName, xmlName );
        }
        
        return xmlName;
    }
}








