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
package org.glassfish.admin.amx.base;


import javax.management.ObjectName;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.api.amx.AMXMBeanMetadata;

/**
    The Pathnames MBean--utilities for working with pathnames and MBeans.
    @since GlassFish V3
 */
@Taxonomy(stability = Stability.UNCOMMITTED)
@AMXMBeanMetadata(type="paths", leaf=true, singleton=true)
public interface Pathnames extends AMXProxy, Utility, Singleton
{
    /** delimiter between parts of a path */
    public static final char SEPARATOR = '/';
    
    /** subscript left character, subscripts must be a character pair for grammar reasons */
    public static final char SUBSCRIPT_LEFT = '[';
    public static final char SUBSCRIPT_RIGHT = ']';
    
    /** A restricted set of characters legal to use as the type portion of a pathname,
        expressed as regex compatible string */
    final String LEGAL_TYPE_CHARS = "a-zA-Z0-9_-";
    
    /** Resolve a path to an ObjectName.  Any aliasing, etc is dealt with.  Return null if failure. */
    @ManagedOperation
    public ObjectName  resolvePath( final String path );
    
    /** Paths that don't resolve result in a null entry */
    @ManagedOperation
    public ObjectName[]  resolvePaths( final String[] paths );
    
    /**
        An efficient way to get the list of MBeans from DomainRoot on down to the specified
        MBean.  The last entry will be the same as the parameter.
        From the ObjectNames one can obtain the path of every ancestor.
        If the MBean does not exist, null will be returned.
     */
    @ManagedOperation
    public ObjectName[] ancestors( final ObjectName objectName );

    /**
        Resolves the path to an ObjectName, then calls ancestors(objectName).
        Any aliasing or special handling will be dealt with.
     */
    @ManagedOperation
    public ObjectName[] ancestors( final String path );
    
    /**
        List descendant ObjectNames.
     */
    @ManagedOperation
    public ObjectName[]  listObjectNames( final String path, final boolean recursive);
    
    /**
        List descendant paths.
     */
    @ManagedOperation
    public String[] listPaths( final String path, boolean recursive );
    
    @ManagedAttribute
    public String[] getAllPathnames();
    
    @ManagedOperation
    public String[] dump( final String path );
}







