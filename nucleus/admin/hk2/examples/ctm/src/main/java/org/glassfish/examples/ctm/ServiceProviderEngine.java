/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.examples.ctm;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jvnet.hk2.annotations.Service;

/**
 * In the example, this code uses the Environment object, which sometimes
 * will be from Tenant1, and other times from Tenant2.  However, since this
 * class is in the Singleton scope, the Environment object cannot be re-injected.
 * Not to fear, because the Environment object is produced as part of a Proxiable
 * scope so the injected entity is actually a proxy.  Hence, when this service
 * uses the Environment object when Tenant1 is in effect it will get the values
 * for Tenant1, and when Tenant2 is in effect it will get the values for Tenant2.
 * 
 * @author jwells
 *
 */
@Service @Singleton
public class ServiceProviderEngine {
    // This is done with a final field to demonstrate that
    // the object here is never modified
    private final Environment environment;
    
    @Inject
    private ServiceProviderEngine(Environment environment) {
        this.environment = environment;
    }
    
    public String getTenantName() {
        return environment.getName();
    }
    
    public int getTenantMin() {
        return environment.getMinSize();
    }
    
    public int getTenantMax() {
        return environment.getMaxSize();
    }

    public Environment getEnvironment() {
        return environment;
    }
}
