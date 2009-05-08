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

import org.glassfish.admin.amx.annotation.Stability;
import org.glassfish.admin.amx.annotation.Taxonomy;


/**
    Interface for AMX debug support remotely.
 */
@Taxonomy(stability = Stability.NOT_AN_INTERFACE)
public interface AMXDebugSupportMBean
{
    public static final String OBJECT_NAME  = "amx-support:name=debug";
    
    /**
        @return default AMX debug state
     */
    public boolean  getDefaultDebug();
    
    /**
        Set the default AMX debug state for any new debug outputs.
     */
    public void     setDefaultDebug( boolean enabled ); 
    
    /**
        @return names of all Outputs in use
     */
    public String[]  getOutputIDs( ); 
    
    
    /**
        @return the debug state for the specified ID
     */
    public boolean  getDebug( final String id ); 
    
    /**
        Set the debug state for the specified ID.
     */
    public void     setDebug( final String id, final boolean enabled ); 
    
    /**
        Set the AMX debug state for all existing new debug outputs.
        These may or may not be MBeans, and an MBean may or may
        not be in debugging mode and using its output.
     */
    public void     setAll( final boolean debug );
    
    /**
        Get rid of all output files.
     */
    public void     cleanup();
    
    /**
        Get the output for the specified ID.
     */
    public String   getOutputFrom( final String id );
}








