/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * Implementations of this interface can be added to a {@link DynamicConfiguration}
 * in order to atomically participate in the changes being made to the
 * {@link ServiceLocator}.  No changes to the ServiceLocator can be made from
 * any method of this interface, otherwise the {@link ServiceLocator} can be
 * left in an inconsistent state
 * 
 * @author jwells
 *
 */
@Contract
public interface TwoPhaseResource {
    /**
     * This method is called prior to any changes being made to the {@link ServiceLocator}
     * but after the IdempotentFilters are called.  If this method throws any exception the
     * entire transaction will not go forward and the thrown exception will be thrown back
     * to the caller.  If this method completes successfully then either the commit or rollback
     * methods will be called eventually once the final outcome of the transaction has been
     * established
     * 
     * @param dynamicConfiguration Information about the dynamic configuration for which this resource
     * was registered
     * @throws MultiException If for some reason the transaction can not go through the expected
     * exception is a MultiException with enclosed exceptions detailing the reasons why the
     * transaction cannot complete.  No subsequent TwoPhaseResource listeners will be invoked
     * once any TwoPhaseResource throws any exception
     */
    public void prepareDynamicConfiguration(TwoPhaseTransactionData dynamicConfiguration) throws MultiException;
    
    /**
     * Once all TwoPhaseResource prepare methods have completed successfully the activate method
     * will be called on all registered TwoPhaseResource implementations.  Any exception from
     * this method will be ignored (though they will be logged if debug logging is turned on).
     * 
     * @param dynamicConfiguration Information about the dynamic configuration for which this resource
     * was registered
     */
    public void activateDynamicConfiguration(TwoPhaseTransactionData dynamicConfiguration);
    
    /**
     * If any TwoPhaseResource fails then all TwoPhaseResources that successfully completed their
     * prepare method will get this method invoked.  Any exceptions from this method will be ignored
     * (though they will be logged if debugging is turned on).
     * 
     * @param dynamicConfiguration Information about the dynamic configuration for which this resource
     * was registered
     */
    public void rollbackDynamicConfiguration(TwoPhaseTransactionData dynamicConfiguration);
}
