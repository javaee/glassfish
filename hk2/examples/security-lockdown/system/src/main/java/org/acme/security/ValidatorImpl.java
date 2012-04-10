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
package org.acme.security;

import java.io.File;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.security.ProtectionDomain;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.Operation;
import org.glassfish.hk2.api.Validator;

/**
 * This is the true validator, and will run against every lookup
 * and injection.
 * <p>
 * In order to bind or unbind services the caller must have AllPermission
 * <p>
 * TODO:  What is a good permission for lookup?
 * 
 * @author jwells
 *
 */
public class ValidatorImpl implements Validator {
    private final static String ACCESS_IN_PACKAGE = "accessClassInPackage.";
    
    /**
     * Ensures that anyone who wants to bind or unbind services has AllPermission
     * 
     * @return true if the caller on the stack has AllPermission
     */
    private boolean validateBindAndUnbind() {
        return checkPerm(new AllPermission());
    }
    
    private boolean validateLookup(ActiveDescriptor<?> candidate, Injectee injectee) {
        if (injectee == null) {
            // This is a raw lookup, and hence we are on the stack.  Now we need to see
            // if the caller is allowed to see the given package
            boolean retVal = checkPerm(getLookupPermission(candidate));
            if (!retVal) {
                System.out.println("candidate " + candidate +
                        " LOOKUP FAILED the security check");
                File hello = new File("policy.txt");
                System.out.println("JRW(10) hello exists? " + hello.exists() + " at " + hello.getAbsolutePath());
                
            }
            return retVal;
        }
        
        // If this is an Inject, get the protection domain of the injectee
        Class<?> injecteeClass = injectee.getInjecteeClass();
        ProtectionDomain pd = injecteeClass.getProtectionDomain();
        Package p = injecteeClass.getPackage();
        
        boolean retVal = pd.implies(getLookupPermission(p.getName()));
        if (!retVal) {
            System.out.println("candidate " + candidate + " injectee " + injectee +
                    " LOOKUP FAILED the security check");
        }
        return retVal;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Validator#validate(org.glassfish.hk2.api.Operation, org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.Injectee)
     */
    @Override
    public boolean validate(Operation operation, ActiveDescriptor<?> candidate,
            Injectee injectee) {
        switch(operation) {
        case BIND:
        case UNBIND:
            return validateBindAndUnbind();
        case LOOKUP:
            return validateLookup(candidate, injectee);
        default:
            return false;
        }
    }
    
    private static Permission getLookupPermission(String packName) {
        RuntimePermission retVal = new RuntimePermission(ACCESS_IN_PACKAGE + packName);
        
        return retVal;
    }
    
    private static Permission getLookupPermission(ActiveDescriptor<?> ad) {
        String fullImplClass = ad.getImplementation();
        int index = fullImplClass.lastIndexOf('.');
        String packName = fullImplClass.substring(0, index);
        return getLookupPermission(packName);
    }
    
    private static boolean checkPerm(Permission p) {
        try {
            AccessController.checkPermission(p);
            return true;
        }
        catch (AccessControlException ace) {
            return false;
        }
    }

}
