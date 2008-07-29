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
package com.sun.appserv.management.base;

import java.util.Map;
import java.util.Set;

/**
    Mixin interface indicating that this item participates in a path-name hierarchy.
    @since Glassfish V3
 */
public interface PathnameSupport
{
    /**
        This character separates the path portion from the attribute name portion.
     */
    public static final String ATTR_NAME_SEP = "@";
    
    public static final char WILDCHARD     = '*';
    
    /**
        Return the pathname type for this AMX MBean.
        <p>
        The name part is escaped as necessary.  For example, if the name is
        "com/foo/Bar", then the the escaped value might be com\/foo\/Bar or "com.foo.bar" (quotes included).
        
        @return the name part, suitably escaped
     */
    public String getPathnameType();
    
    /**
        The name part (if any) is escaped as necessary.  The PathnameName will be the same as
        the result of getName(), except that it will be null if the item is a singleton.
        
        @return the name part
        @see AMX#getName
     */
    public String getPathnameName();
    
    /** the composition of the type and name eg bar or foo[name] */
    public String getPathnamePart();
    
    /**
        Return the entire path name for this AMX MBean, suitably escaped.
        
        @see #getDottedNamePart
        @return the path name
     */
    public String getPathname();
    
    /**
        Return the value of a path name.
        @param valueName the name of the value *only* (does not include the path portion)
        @return the value.  An exception is thrown if valueName is illegal
     */
    public String getPathnameValue( final String valueName );
    
    /**
        Return the value of path names.  If a value cannot be obtained, it is not returned
        in the Map; only values obtained successfully are returned.
        <p>
        A null Set indicates that all values should be returned.
        @see getDottedValueNames
     */
    public Map<String,String> getPathnameValues( final Set<String> values );
    
    /**
        Return a Map from path-value names to Attribute names.
     */
    public Map<String,String> getPathnameToAttributes();
}



