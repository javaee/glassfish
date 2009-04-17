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
package org.glassfish.admin.amx.impl.j2ee;

import javax.management.MBeanServer;
import org.glassfish.admin.amx.j2ee.J2EEDomain;
import org.glassfish.admin.amx.impl.util.Issues;

import javax.management.ObjectName;
import org.glassfish.admin.amx.core.Util;
import org.glassfish.admin.amx.impl.util.ImplUtil;
import org.glassfish.admin.amx.impl.util.ObjectNames;
import org.glassfish.admin.amx.j2ee.J2EEServer;
import org.glassfish.admin.amx.j2ee.J2EETypes;

/**
Base implementation for the J2EEDomain for DAS and non-DAS server instances.
 */
public class J2EEDomainImpl extends J2EEManagedObjectImplBase {

    public J2EEDomainImpl(final ObjectName parentObjectName) {
        super(parentObjectName, J2EEDomain.class);

        Issues.getAMXIssues().notDone("J2EEDomainImpl needs to account for DAS/non-DAS");
    }

    /**
    JSR 77 impl
    @return String representation of the ObjectName
     */
    public String[] getservers() {
        return getChildrenAsStrings( J2EETypes.J2EE_SERVER );
    }
    
    @Override
    protected String getExtraObjectNameProps(final MBeanServer server, final ObjectName nameIn)
    {
        // SPECIAL CASE per JSR 77 spec:
        // add the 'name' property even though this is a singleton
        String props = super.getExtraObjectNameProps(server, nameIn);
        final String nameProp = Util.makeNameProp( nameIn.getDomain() );
        props = Util.concatenateProps( props, nameProp );

        return props;
    }
    
    @Override
        protected void
    registerChildren()
    {
        final ObjectNames builder = getObjectNames();

        final DASJ2EEServerImpl impl = new DASJ2EEServerImpl( getObjectName() );
        final String serverName = "das";
        ObjectName serverObjectName = getObjectNames().buildChildObjectName( J2EEServer.J2EE_TYPE, serverName );
        serverObjectName = registerChild( impl, serverObjectName );
        ImplUtil.getLogger().info( "Registered J2EEDomain as " + getObjectName() + " with J2EEServer of " + serverObjectName);
    }
    /*
v3:path=/J2EEDomain,type=J2EEDomain,name=v3
v3:path=/J2EEDomain/J2EEServer[das],type=J2EEServer,name=das,j2eeType=J2EEServer,null=v3
v3:path=/J2EEDomain/JVM,type=JVM,j2eeType=JVM,null=v3,J2EEServer=das
*/
}


