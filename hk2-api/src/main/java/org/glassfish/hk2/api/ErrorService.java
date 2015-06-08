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
package org.glassfish.hk2.api;

import org.jvnet.hk2.annotations.Contract;

/**
 * This interface should be implemented by those who wish to be
 * notified of error conditions that occur within HK2.  These
 * errors are those that might happen during normal processing of
 * HK2 requests
 * <p>
 * An implementation of ErrorService must be in the Singleton scope.
 * Implementations of ErrorService will be instantiated as soon as
 * they are added to HK2 in order to avoid deadlocks and circular references.
 * Therefore it is recommended that implementations of ErrorService
 * make liberal use of {@link javax.inject.Provider} or {@link IterableProvider}
 * when injecting dependent services so that these services are not instantiated
 * when the ErrorService is created
 * 
 * @author jwells
 *
 */
@Contract
public interface ErrorService {
    /**
     * This method is called when a failure occurs in the system.  This method may
     * use any {@link ServiceLocator} api.  For example, an implementation of this method might want
     * to remove a descriptor from the registry if the error can be determined to be a
     * permanent failure.
     * 
     * @param errorInformation Information about the error that occurred
     * @throws MultiException if this method throws an exception that exception will be thrown back to
     * the caller wrapped in another MultiException if the error is of type {@link ErrorType#FAILURE_TO_REIFY}.
     * If the error is of type {@link ErrorType#DYNAMIC_CONFIGURATION_FAILURE} or {@link ErrorType#SERVICE_CREATION_FAILURE}
     * or {@link ErrorType#SERVICE_DESTRUCTION_FAILURE} then any exception thrown from this
     * method is ignored and the original exception is thrown back to the caller
     */
    public void onFailure(ErrorInformation errorInformation)
        throws MultiException;

}
