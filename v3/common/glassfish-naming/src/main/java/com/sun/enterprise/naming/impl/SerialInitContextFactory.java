/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.naming.impl;

import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.glassfish.api.naming.NamingObjectsProvider;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.component.Habitat;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implements the JNDI SPI InitialContextFactory interface used to create
 * the InitialContext objects. It creates an instance of the serial context.
 */

public class SerialInitContextFactory implements InitialContextFactory {


    private Hashtable defaultEnv;
    private Habitat habitat;

    private boolean useS1ASCtxFactory;

    private static AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Default constructor. Creates an ORB if one is not already created.
     */
    public SerialInitContextFactory() {
        habitat = Globals.getDefaultHabitat(); // TODO fix this
    }
    
    /**
     * Create the InitialContext object.
     */
    public Context getInitialContext(Hashtable env) throws NamingException {

        //Another Big TODO Sync with useS1ASCtxFactory

        // Use Atomic look to ensure only first thread does NamingObjectsProvider
        // initialization.
        // TODO Note that right now the 2nd, 3rd. etc. threads will proceed
        // past here even if the first thread is still doing its getAllByContract
        // work.  Should probably change the way this works to eliminate that
        // time window where the objects registered by NamingObjectsProvider
        // aren't available.
        if( !initialized.get() ) {
            boolean firstToInitialize = initialized.compareAndSet(false, true);
            
            if (firstToInitialize) {

                // this should force the initialization of the resources providers
                if (habitat!=null) {
                    for (NamingObjectsProvider provider :
                            habitat.getAllByContract(NamingObjectsProvider.class)) {
                        // no-op
                    }
                }
            }
        }

        if (env != null) {
            return new SerialContext(env, habitat);
        } else {
            return new SerialContext(defaultEnv, habitat);
        }
    }
}
