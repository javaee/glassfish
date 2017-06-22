/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.deployment.admin;

import java.util.Arrays;
import java.util.Collection;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.deployment.ApplicationLifecycleInterceptor;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;

/**
 *
 * @author Tim Quinn
 */
public class InterceptorNotifier {

    private boolean[] isBeforeReported = initialIsReported(); 
    private boolean[] isAfterReported = initialIsReported();
    
    private Collection<ApplicationLifecycleInterceptor> interceptors;
    private ExtendedDeploymentContext dc = null;
    
    private static boolean[] initialIsReported() {
        final boolean[] result = new boolean[ExtendedDeploymentContext.Phase.values().length];
        Arrays.fill(result, false);
        return result;
    }

    public InterceptorNotifier(final ServiceLocator habitat, final DeploymentContext basicDC) {
        if (basicDC != null) {
            if (! (basicDC instanceof ExtendedDeploymentContext)) {
                throw new IllegalArgumentException(basicDC.getClass().getName());
            }
            dc = ExtendedDeploymentContext.class.cast(basicDC);
        }
        interceptors = habitat.getAllServices(ApplicationLifecycleInterceptor.class);
    }

    synchronized void ensureBeforeReported(final ExtendedDeploymentContext.Phase phase) {
        if (isBeforeReported[phase.ordinal()]) {
            return;
        }
        for (ApplicationLifecycleInterceptor i : interceptors) {
            i.before(phase, dc);
        }
        isBeforeReported[phase.ordinal()] = true;
    }
    
    synchronized void ensureAfterReported(final ExtendedDeploymentContext.Phase phase) {
        if (isAfterReported[phase.ordinal()]) {
            return;
        }
        
        for (ApplicationLifecycleInterceptor i : interceptors) {
            i.after(phase, dc);
        }
        isAfterReported[phase.ordinal()] = true;
    }

    ExtendedDeploymentContext dc() {
        return dc;
    }    
}
