/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2014 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.runlevel;

import org.glassfish.hk2.api.AnnotationLiteral;
import org.glassfish.hk2.api.DuplicateServiceException;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.internal.AsyncRunLevelContext;
import org.glassfish.hk2.runlevel.internal.RunLevelControllerImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * Utilities for using the RunLevelService
 * 
 * @author jwells
 *
 */
public class RunLevelServiceUtilities {
    /**
     * Enables the RunLevelService in the given {@link ServiceLocator}.
     * If the {@link RunLevelContext} is already registered then
     * this method does nothing.
     * <p>
     * All services needed by the
     * RunLevelService feature are marked with {@link Service} and
     * hence would be automatically picked up in environments that
     * use automatic service discovery
     * 
     * @param locator the non-null service locator to add
     * the run-level service to
     */
    public static void enableRunLevelService(ServiceLocator locator) {
        if (locator.getService(RunLevelContext.class) != null) return;
        
        try {
            ServiceLocatorUtilities.addClasses(locator, true,
                RunLevelContext.class,
                AsyncRunLevelContext.class,
                RunLevelControllerImpl.class);
        }
        catch (MultiException me) {
            if (!isDupException(me)) throw me;
        }
    }
    
    private static boolean isDupException(MultiException me) {
        boolean atLeastOne = false;
        
        for (Throwable error : me.getErrors()) {
            atLeastOne = true;
            
            if (!(error instanceof DuplicateServiceException)) return false;
        }
        
        return atLeastOne;
    }
    
    /**
     * Returns a {@link RunLevel} scope annotation with the
     * given value and RUNLEVEL_MODE_VALIDATING as the mode
     * 
     * @param value The value this RunLevel should take
     * @return A {@link RunLevel} scope annotation
     */
    public static RunLevel getRunLevelAnnotation(int value) {
        return getRunLevelAnnotation(value, RunLevel.RUNLEVEL_MODE_VALIDATING);
        
    }
    
    /**
     * Returns a {@link RunLevel} scope annotation with the
     * given value and mode
     * 
     * @param value The value this RunLevel should take
     * @param mode The mode the RunLevel should take:<UL>
     * <LI>RUNLEVEL_MODE_VALIDATING</LI>
     * <LI>RUNLEVEL_MODE_NON_VALIDATING</LI>
     * </UL>
     * @return A {@link RunLevel} scope annotation
     */
    public static RunLevel getRunLevelAnnotation(int value, int mode) {
        return new RunLevelImpl(value, mode);
    }
    
    private static class RunLevelImpl extends AnnotationLiteral<RunLevel> implements RunLevel {
        private static final long serialVersionUID = -359213687920354669L;
        
        private final int value;
        private final int mode;
        
        private RunLevelImpl(int value, int mode) {
            this.value = value;
            this.mode = mode;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.runlevel.RunLevel#value()
         */
        @Override
        public int value() {
            return value;
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.runlevel.RunLevel#mode()
         */
        @Override
        public int mode() {
            return mode;
        }

        
    }

}
