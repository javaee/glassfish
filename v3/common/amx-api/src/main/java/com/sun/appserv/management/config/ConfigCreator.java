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
package com.sun.appserv.management.config;

import com.sun.appserv.management.base.AMXAttributes;
import com.sun.appserv.management.base.Container;

import java.util.Map;

/**
	Any {@link AMXConfig} implementing this interface allows any contained AMXConfig to be created.
    Implementors of this interface should generally also be an {@link Container}, but certain
    special types might not be.
*/
public interface ConfigCreator
{
    /**
        Generic creation of an {@link AMXConfig} based on the desired j2eeType, which must
        be a Containee type of the {@link Container} (eg one of the types returned by {@link Container#getContaineeSet}.
        <p>
        Almost always, certain parameters are required (a name for non-singletons).  Use the
        {@link AMXAttributes#ATTR_NAME} key for the name.
        <p>
        Properties can be included in the 'params' Map using the {@link PropertiesAccess#PROPERTY_PREFIX}
        prefix on the property name.  
        System properties can be included in the 'params' Map using the
        {@link SystemPropertiesAccess#SYSTEM_PROPERTY_PREFIX} prefix on the property name.
        
        @param j2eeType the AMX j2eeType
        @param optional optional parameters (some of which may be reu
        @return proxy interface to the newly-created AMXConfig
        
        @see com.sun.appserv.management.base.XTypes
     */
    public <T extends AMXConfig > T createAMXConfig( String j2eeType, Map<String,String> params );
    
    /**
        Dynamically add an interface which can be instantiated.  The interface must extend
        {@link org.jvnet.hk2.config.ConfigBeanProxy}.
     */
    public void addCreatable( final String interfaceName );
}
