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
package com.sun.enterprise.ee.synchronization.impl;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.ee.synchronization.api.SynchronizationContext;
import com.sun.enterprise.ee.synchronization.api.ApplicationsMgr;
import com.sun.enterprise.ee.synchronization.api.SecurityServiceMgr;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * Synchronization context. It provides access to the synchronization 
 * managers. The managers allow convenient APIs to synchronize (pull)
 * content from the central repository via domain administration 
 * server (DAS).
 *
 * <xmp>
 * Example: The following code snippet shows how to get hold of 
 * synchronization managers.
 *
 *   // config context
 *   ConfigContext configContext = event.getConfigContext();
 *
 *   // creates a synchronization context
 *   SynchronizationContext synchCtx = 
 *       SynchronizationFactory.createSynchronizationContext(configContext);
 *
 *   // applications synchronization manager
 *   ApplicationsMgr appSynchMgr = synchCtx.getApplicationsMgr();
 *
 *   // security service synchronization manager
 *   SecurityServiceMgr securitySynchMgr = synchCtx.getSecurityServiceMgr();
 * </xmp>
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class SynchronizationContextImpl implements SynchronizationContext {

    /**
     * Default constructor!
     * 
     * <p> WARNING: Must call setConfigContext before calling the methods.
     */
    public SynchronizationContextImpl() { }

    /**
     * Sets the config context.
     *
     * @param  ctx  config context
     */
    public void setConfigContext(ConfigContext ctx) {
        _ctx = ctx;
    }

    /** 
     * Returns the associated config context for this synchronization context.
     *
     * @return  the associated config context for this synchronization context
     */
    public ConfigContext getConfigContext() {
        return _ctx;
    }

    /**
     * Constructor!
     *
     * @param  ctx  config context
     */
    public SynchronizationContextImpl(ConfigContext ctx) {
        _ctx = ctx;
    }

    /**
     * Returns the synchronization manager for applications.
     *
     * @return   synchronization manager for applications
     */
    public ApplicationsMgr getApplicationsMgr() {

        if (_ctx == null) {
            throw new RuntimeException(_localStrMgr.getString(
                "ConfigCtxNotSet"));
        }
        return new ApplicationsMgrImpl(_ctx);
    }

    /**
     * Returns the synchronization manager for security service.
     * 
     * @return   synchronization manager for security service
     */
    public SecurityServiceMgr getSecurityServiceMgr() {

        if (_ctx == null) {
            throw new RuntimeException(_localStrMgr.getString(
                "ConfigCtxNotSet"));
        }
        return new SecurityServiceMgrImpl(_ctx);
    }

    // ---- VARIABLES - PRIVATE -------------------------------------------
    private ConfigContext _ctx = null;
    private static final StringManager _localStrMgr =
        StringManager.getManager(SynchronizationContextImpl.class);

}
