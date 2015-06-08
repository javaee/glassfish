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

package org.glassfish.examples.caching.services;

import javax.inject.Inject;

import org.glassfish.examples.caching.hk2.Cache;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * This is a service that has a very expensive constructor.  Luckily
 * this service can be cached with the constructor input
 * parameter as a key, and so we will use constructor interception with
 * it
 * 
 * @author jwells
 *
 */
@Service @PerLookup
public class ExpensiveConstructor {
    private static int numTimesConstructed;
    private final int multiplier;
    
    /**
     * This is the extremely expensive constructor
     * 
     * @param multiplier The number to multiply by
     */
    @Inject @Cache
    public ExpensiveConstructor(int multiplier) {
        // Very expensive operation
        this.multiplier = multiplier * 2;
        numTimesConstructed++;
    }
    
    /**
     * This method ensures that we can get at the
     * results of the expensive operation performed
     * in the constructor
     * 
     * @return The result of the expensive operation
     * done in the constructor
     */
    public int getComputation() {
        return multiplier;
    }
    
    /**
     * Clears the number of times this was constructed
     */
    public static void clear() {
        numTimesConstructed = 0;
    }
    
    /**
     * Gets the number of times this class has been
     * constructed since the last call to {@link #clear()}
     * 
     * @return The number of times this class has been constructed
     */
    public static int getNumTimesConstructed() {
        return numTimesConstructed;
    }
}
