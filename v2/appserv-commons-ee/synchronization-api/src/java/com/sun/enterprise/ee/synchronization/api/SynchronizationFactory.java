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
package com.sun.enterprise.ee.synchronization.api;

import java.lang.reflect.Constructor;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.ee.synchronization.SynchronizationException;

/**
 * Factory class for synchronization API.
 *
 * <xmp>
 * Example: The following code snippet shows how to create a 
 * synchronization context.
 *
 *   // config context
 *   ConfigContext configContext = event.getConfigContext();
 *
 *   // creates a synchronization context
 *   SynchronizationContext synchCtx = 
 *       SynchronizationFactory.createSynchronizationContext(configContext);
 *
 * </xmp>
 *
 * @author Nazrul Islam
 * @since  JDK1.4
 */
public class SynchronizationFactory {

    /**
     * Creates a new synchronization context from the given config context.
     *
     * @param  ctx  config context of the server. Server acts as a client 
     *     and synchronizes (pulls) content from the central repository via
     *     domain administration server (DAS).
     *
     * @return  synchronization context for the server
     *
     * @throws  SynchronizationException  if an error while creating 
     *     synchronization context
     */
    public static SynchronizationContext createSynchronizationContext(
            ConfigContext ctx) throws SynchronizationException {

        SynchronizationContext sCtx = null;

        try {
            // instanciates a new instance of the synchronization context
            Class c = Class.forName(SYNCHRONIZATION_CONTEXT_IMPL);
            sCtx = (SynchronizationContext) c.newInstance();

            // sets the config context
            sCtx.setConfigContext(ctx);

        } catch (Exception e) {
            String msg = _localStrMgr.getString("SynchCtxCreationError");
            throw new SynchronizationException(msg, e);
        }

        return sCtx;
    }

    /**
     * Creates a synchronization client. Synchronization client is a 
     * FTP like service where a file (or jar) can be put or downloaded 
     * to/from a remote server.
     *
     * @param  remoteServerName   name of the remote server name
     *
     * @throws SynchronizationException  if an error while creating the client
     */
    public static SynchronizationClient createSynchronizationClient(
            String remoteServerName) throws SynchronizationException {

        SynchronizationClient client = null;

        try {
            Class c = Class.forName(SYNCHRONIZATION_CLIENT_IMPL);
            Constructor constructor = 
                c.getConstructor(new Class[] {java.lang.String.class});

            // new instance of synchronization client
            client = (SynchronizationClient) 
                constructor.newInstance(new Object[] {remoteServerName});

        } catch (Exception e) {
            String msg = _localStrMgr.getString("SynchClientCreationError");
            throw new SynchronizationException(msg, e);
        }

        return client;
    }

    // ---- VARIABLES - PRIVATE -------------------------------------------
    private static final String SYNCHRONIZATION_CONTEXT_IMPL =
        "com.sun.enterprise.ee.synchronization.impl.SynchronizationContextImpl";
    private static final String SYNCHRONIZATION_CLIENT_IMPL  = 
        "com.sun.enterprise.ee.synchronization.impl.SynchronizationClientImpl";
    private static final StringManager _localStrMgr =
        StringManager.getManager(SynchronizationFactory.class);
}
