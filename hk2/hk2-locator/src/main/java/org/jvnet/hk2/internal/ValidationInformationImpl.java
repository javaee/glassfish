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
package org.jvnet.hk2.internal;

import java.util.HashSet;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.Operation;
import org.glassfish.hk2.api.ValidationInformation;

/**
 * @author jwells
 *
 */
public class ValidationInformationImpl implements ValidationInformation {
    private final static String SERVICE_LOCATOR_IMPL = "org.jvnet.hk2.internal.ServiceLocatorImpl";
    private final static String VALIDATE_METHOD = "validate";
    private final static String CHECK_METHOD = "checkConfiguration";
    
    private final static String[] SKIP_ME = {
        "org.jvnet.hk2.internal",
        "org.jvnet.hk2.external.generator",
        "org.glassfish.hk2.extension",
        "org.glassfish.hk2.api",
        "org.glassfish.hk2.internal",
        "org.glassfish.hk2.utilities",
        "org.glassfish.hk2.utilities.binding",
        "org.jvnet.hk2.annotations",
        "org.glassfish.hk2.utilities.cache",
        "org.glassfish.hk2.utilities.cache.internal",
        "org.glassfish.hk2.utilities.reflection",
        "org.jvnet.hk2.component"
    };
    
    private final static HashSet<String> PACKAGES_TO_SKIP = new HashSet<String>();
    
    static {
        for (String pack : SKIP_ME) {
            PACKAGES_TO_SKIP.add(pack);
        }
    }
    private final Operation operation;
    private final ActiveDescriptor<?> candidate;
    private final Injectee injectee;
    private final Filter filter;
    
    /**
     * Creates the validation information
     * @param operation The operation to perform
     * @param candidate The candidate to perform it on
     * @param injectee The injecteee that may be involved
     * @param filter The filter that may be involved in the lookup
     */
    public ValidationInformationImpl(Operation operation,
            ActiveDescriptor<?> candidate,
            Injectee injectee,
            Filter filter) {
        this.operation = operation;
        this.candidate = candidate;
        this.injectee = injectee;
        this.filter = filter;
    }
    
    /**
     * Creates the validation information
     * @param operation The operation to perform
     * @param candidate The candidate to perform it on
     */
    public ValidationInformationImpl(Operation operation,
            ActiveDescriptor<?> candidate) {
        this(operation, candidate, null, null);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ValidationInformation#getOperation()
     */
    @Override
    public Operation getOperation() {
        return operation;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ValidationInformation#getCandidate()
     */
    @Override
    public ActiveDescriptor<?> getCandidate() {
        return candidate;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ValidationInformation#getInjectee()
     */
    @Override
    public Injectee getInjectee() {
        return injectee;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.ValidationInformation#getFilter()
     */
    @Override
    public Filter getFilter() {
        return filter;
    }
    
    private String getPackage(String name) {
        int index = name.lastIndexOf('.');
        if (index < 0) return name;
        
        return name.substring(0, index);
    }
    
    /**
     * The implementation of this method is VERY dependent on the implementation
     * of ServiceLocatorImpl.  Any refactoring of ServiceLocatorImpl having to
     * do with validation will likely cause this code to break.  Hopefully the
     * unit tests will catch any such failures!
     */
    @Override
    public StackTraceElement getCaller() {
        StackTraceElement frames[] = Thread.currentThread().getStackTrace();
        
        boolean foundValidationCaller = false;
        for (StackTraceElement e : frames) {
            if (!foundValidationCaller) {
                if (SERVICE_LOCATOR_IMPL.equals(e.getClassName()) &&
                        (VALIDATE_METHOD.equals(e.getMethodName()) ||
                         CHECK_METHOD.equals(e.getMethodName()))) {
                    foundValidationCaller = true;
                }
            }
            else {
                String pack = getPackage(e.getClassName());
                if (!PACKAGES_TO_SKIP.contains(pack)) return e;
            }
        }
        
        return null;
    }

    public String toString() {
        return "ValidationInformation(" + operation + "," +
            candidate + "," +
            injectee + "," +
            filter + "," +
            System.identityHashCode(this) + ")";
    }
}
