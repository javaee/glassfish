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
 * This object contains information about the validation
 * point.  The values available may vary depending on
 * the type of operation.
 * 
 * @author jwells
 *
 */
public interface ValidationInformation {
    /**
     * The operation that is to be performed, one of<UL>
     * <LI>BIND - The candidate descriptor is being added to the system</LI>
     * <LI>UNBIND - The candidate descriptor is being removed from the system</LI>
     * <LI>LOOKUP - The candidate descriptor is being looked up</LI>
     * </UL>
     * 
     * @return The operation being performed
     */
    public Operation getOperation();
    
    /**
     * The candidate descriptor for this operation
     * 
     * @return The candidate descriptor for the operation being performed
     */
    public ActiveDescriptor<?> getCandidate();
    
    /**
     * On a LOOKUP operation if the lookup is being performed due to an
     * injection point (as opposed to a lookup via the API) then this
     * method will return a non-null {@link Injectee} that is the injection
     * point that would be injected into
     * 
     * @return The injection point being injected into on a LOOKUP operation
     */
    public Injectee getInjectee();
    
    /**
     * On a LOOKUP operation the {@link Filter} that was used in the
     * lookup operation.  This may give more information about what
     * exactly was being looked up by the caller
     * 
     * @return The filter used in the lookup operation
     */
    public Filter getFilter();
    
    /**
     * This method attempts to return the StackTraceElement
     * of the code calling the HK2 method that caused
     * this validation to occur
     * <p>
     * This method may not work properly if called outside
     * of the call frame of the {@link Validator#validate(ValidationInformation)}
     * method
     * 
     * @return The caller of the HK2 API that caused this
     * validation to occur, or null if the caller could
     * not be determined
     */
    public StackTraceElement getCaller();

}
