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
package com.sun.appserv.management.helper;

import com.sun.appserv.management.config.AttributeResolver;

/**
	Helper to resolve attribute configuration values eg ${com.sun.aas.installRoot} once they have
    already been obtained in "raw" form.  If the goal is to fetch the attribute values in
    already-resolved form, do so directly via @{link AttributeResolver#resolveAttribute}.
    <p>
    Values can be resolved into String, boolean or int.  
    <p>
    Example usage:</b>
    <pre>
    HTTPListenerConfig l = ...; // or any AMXConfig sub-interface
    AttributeResolverHelper h = new AttributeResolverHelper( l );
    int port = h.resolveInt( l.getPort() );
    </pre>
    Alternately, the static method form can be used:<br>
    <pre>
    HTTPListenerConfig l = ...; // or any AMXConfig sub-interface
    int port = AttributeResolverHelper.resolveInt( l, value );
    </pre>
    The value can also be pre-resolved by calling {@link AttributeResolver#resolveAttribute}
    @see com.sun.appserv.management.config.AttributeResolver
 */
public class AttributeResolverHelper
{
    private final AttributeResolver mResolver;
    
    /**
        An AttributeResolver will usually be an {@link com.sun.appserv.management.config.AMXConfig},
        but could be another implementation if desired.
     */
    public AttributeResolverHelper( final AttributeResolver resolver )
    {
        mResolver = resolver;
    }
    
    /**
        Return true if the string is a template string of the for ${...}
     */
        public static boolean
    needsResolving( final String value )
    {
        if ( value == null ) return false;
        
        final String temp = value.trim();
        
        return temp.startsWith( "${" ) && temp.endsWith( "}" );
    }
    
    /**
        Extract the variable name.
     */
        public static String
    extract( final String value )
    {
        // 2 is length of "${" and 1 is for the "}"
        return needsResolving(value) ? value.trim().substring(2, value.length() - 1) : value;
    }
    
    /**
        Resolve the String using the target resolver (MBean).
     */
        public String
    resolve( final String in )
    {
        return resolve( mResolver, in );
    }
    
    /**
        Resolve the String using the specified resolver.
     */
        public static String
    resolve( final AttributeResolver resolver, final String value )
    {
        return needsResolving(value) ? resolver.resolveAttributeValue(value) : value;
    }
    
    /**
        Resolve the String into a boolean value using the target resolver (MBean).
     */
        public boolean
    resolveBoolean(final String value )
    {   
        return resolveBoolean( mResolver, value );
    }
    
    /**
        Resolve the String into a boolean value using the specified resolver.
     */
        public static boolean
    resolveBoolean(
        final AttributeResolver resolver,
        final String           value )
    {
        final String resolved = resolve( resolver, value );
        
        return Boolean.parseBoolean( resolved );
    }

    /**
        Resolve the String into an int value using the target resolver (MBean).
     */
        public int
    resolveInt(final String value )
    {   
        return resolveInt( mResolver, value );
    }
    
    /**
        Resolve the String into an int value using the specified resolver.
     */
        public static int
    resolveInt(
        final AttributeResolver resolver,
        final String           value )
    {
        final String resolved = resolve( resolver, value );
        
        return Integer.parseInt( resolved );
    }
}




