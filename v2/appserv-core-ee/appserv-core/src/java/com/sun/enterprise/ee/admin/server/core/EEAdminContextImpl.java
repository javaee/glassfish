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
package com.sun.enterprise.ee.admin.server.core;

import com.sun.enterprise.admin.server.core.AdminContextImpl;
import com.sun.enterprise.admin.AdminContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.URL;

import com.sun.enterprise.admin.meta.MBeanRegistry;
import com.sun.enterprise.server.ServerContextImpl;

/**
 *
 */
public class EEAdminContextImpl extends AdminContextImpl {

      
    private static final String EE_ADMIN_DESCRIPTORS_FILENAME   = "/admin-mbeans-descriptors-ee.xml";
    private static final String EE_RUNTIME_DESCRIPTORS_FILENAME   = "/runtime-mbeans-descriptors-ee.xml";

    /**
     * Creates a new instance of AdminContextImpl
     */
    public EEAdminContextImpl() {
        super();
        initMBeanRegistryURLs();
    }

    public EEAdminContextImpl(ServerContextImpl srvCtx) {
        super(srvCtx);
        initMBeanRegistryURLs();        
    }

    private void initMBeanRegistryURLs() 
    {
        try { 
            super.setAdminMBeanRegistryURL(MBeanRegistry.class.getResource(
                EE_ADMIN_DESCRIPTORS_FILENAME));
        } catch (Throwable t) {
            super.getAdminLogger().log(Level.WARNING, "core.adminconfig_adminregistry_not_found",
                EE_ADMIN_DESCRIPTORS_FILENAME);
        } 
        try { 
            super.setRuntimeMBeanRegistryURL(MBeanRegistry.class.getResource(
                EE_RUNTIME_DESCRIPTORS_FILENAME));
        } catch (Throwable t) {
            super.getAdminLogger().log(Level.WARNING, "core.adminconfig_runtimeregistry_not_found",
                EE_RUNTIME_DESCRIPTORS_FILENAME);
        }
    }

    public String getDottedNameMBeanImplClassName(){
        return "com.sun.enterprise.ee.admin.mbeans.DottedNameGetSetMBeanImplEE";
    }
}
