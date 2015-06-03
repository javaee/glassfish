/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

/**
 * This class has information in it about the error that
 * has occurred
 * 
 * @author jwells
 *
 */
public interface ErrorInformation {
    /**
     * Gets the type of error that has occurred.  Code should be
     * written such that future error types are handled appropriately.
     * 
     * @return <UL>
     * <LI>{@link ErrorType#FAILURE_TO_REIFY}</LI>
     * <LI>{@link ErrorType#DYNAMIC_CONFIGURATION_FAILURE}</LI>
     * <LI>{@link ErrorType#SERVICE_CREATION_FAILURE}</LI>
     * <LI>{@link ErrorType#SERVICE_DESTRUCTION_FAILURE}</LI>
     * <LI>{@link ErrorType#VALIDATE_FAILURE}</LI>
     * </UL>
     */
    public ErrorType getErrorType();
    
    /**
     * This will contain the active descriptor that is associated
     * with this failure.  In the case of FAILURE_TO_REIFY it will
     * contain the descriptor that failed to reify.  In the
     * DYNAMIC_CONFIGURATION_FAILURE case this will return null.
     * In SERVICE_CREATION_FAILURE and SERVICE_DESTRUCTION_FAILURE
     * it will contain the descriptor whose create or destroy methods
     * failed.  In the case of VALIDATE_FAILURE it will contain
     * the descriptor that failed the security check
     * 
     * @return The descriptor associated with this failure
     */
    public Descriptor getDescriptor();
    
    /**
     * This will contain information about the Injectee that was being
     * injected into when the error occurred.
     * <p>
     * In the case of FAILURE_TO_REIFY this will be the injectee that was
     * being looked up to satisfy the injection point, or null if this lookup
     * was due to an API call.
     * <p>
     * In the case of VALIDATE_FAILURE this will contain the injectee that
     * was being looked up when the failure occurred or null if this was a
     * lookup operation or the injectee is unknown for some other reason
     * <p>
     * In the cases of DYNAMIC_CONFIGURATION_FAILURE, SERVICE_CREATION_FAILURE and
     * SERVICE_DESTRUCTION_FAILURE this will return null.
     * 
     * @return The injectee associated with this failure
     */
    public Injectee getInjectee();
    
    /**
     * This will contain the associated exception or exceptions that caused
     * the failure.
     * <p>
     * In the case of FAILURE_TO_REIFY this will contain the exception that caused
     * the reification process to fail
     * <p>
     * In the case of DYNAMIC_CONFIGURATION_FAILURE this will contain the exception
     * that cause the configuration operation to fail
     * <p>
     * In the case of SERVICE_CREATION_FAILURE this will contain the exception
     * that was thrown during service creation
     * <p>
     * In the case of SERVICE_DESTRUCTION_FAILURE this will contain the exception
     * that was thrown during service destruction
     * <p>
     * In the case of VALIDATE_FAILURE this will contain the exception that was
     * thrown from the {@link Validator#validate(ValidationInformation)} method
     * 
     * @return The exception associated with this failure
     */
    public MultiException getAssociatedException();
}
