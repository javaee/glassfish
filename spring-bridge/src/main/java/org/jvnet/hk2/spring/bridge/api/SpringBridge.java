/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package org.jvnet.hk2.spring.bridge.api;

import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.spring.bridge.internal.SpringBridgeImpl;

/**
 * An adapter for creating a Spring bridge
 * 
 * @author jwells
 *
 */
public abstract class SpringBridge {
    private final static SpringBridge INSTANCE = new SpringBridgeImpl();
    
    /**
     * Returns an instance of the SpringBridge for use in
     * initializing the Spring bridge
     * 
     * @return The SpringBridge instance
     */
    public static SpringBridge getSpringBridge() {
        return INSTANCE;
    }
    
    /**
     * This method will initialize the given service locator for use with the Spring/HK2
     * bridge.  It adds into the service locator an implementation of SpringIntoHK2Bridge
     * and also the custom scope needed for Spring services.  This method is idempotent,
     * in that if these services have already been added to the service locator
     * they will not be added again
     * 
     * @param locator A non-null locator to use with the Spring/HK2 bridge
     * @throws MultiException On failure
     */
    public abstract void initializeSpringBridge(ServiceLocator locator) throws MultiException;
}
