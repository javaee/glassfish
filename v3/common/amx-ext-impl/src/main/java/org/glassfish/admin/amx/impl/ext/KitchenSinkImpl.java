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
package org.glassfish.admin.amx.impl.ext;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import org.glassfish.admin.amx.base.KitchenSink;
import static org.glassfish.admin.amx.base.KitchenSink.*;
import org.glassfish.admin.amx.util.ExceptionUtil;
import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Habitat;

import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.admin.amx.impl.mbean.AMXImplBase;

/**
    
 */
public final class KitchenSinkImpl extends AMXImplBase
	//implements KitchenSink
{
    public KitchenSinkImpl(final ObjectName parentObjectName) {
        super( parentObjectName, KitchenSink.class );
	}
    
        public Map<String,Object>
    getConnectionDefinitionPropertiesAndDefaults( final String datasourceClassName ) {
        final Map<String,Object> result = new HashMap<String,Object>();
        final Habitat habitat = org.glassfish.internal.api.Globals.getDefaultHabitat();

        if (habitat == null) {
            result.put( PROPERTY_MAP_KEY, null );
            result.put( REASON_FAILED_KEY, "Habitat is null");
            return result;
        }

        // get connector runtime
        try {
            final ConnectorRuntime connRuntime = habitat.getComponent(ConnectorRuntime.class, null);
            final Map<String,Object>  connProps = connRuntime.getConnectionDefinitionPropertiesAndDefaults( datasourceClassName );
            result.put( PROPERTY_MAP_KEY, connProps );
        } catch (ComponentException e) {
            result.put( PROPERTY_MAP_KEY, null );
            result.put( REASON_FAILED_KEY, ExceptionUtil.toString(e));
        }
        
        // got everything, now get properties
        return result;
    }
}








