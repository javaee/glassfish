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

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.ValidationInformation;
import org.glassfish.hk2.api.Validator;

/**
 * This is the true validator, and will run against every lookup
 * and injection.
 * <p>
 * In order to bind or unbind services the caller must have AllPermission
 * <p>
 * 
 * @author jwells
 *
 */
public class ValidatorImpl implements Validator {
    private final static boolean VERBOSE = Boolean.parseBoolean(System.getProperty(
            "org.jvnet.hk2.examples.securitylockdown.debug", "false"));
    private final static String ACCESS_IN_PACKAGE = "accessClassInPackage.";
    
    /**
     * Ensures that anyone who wants to bind or unbind services has AllPermission
     * 
     * @return true if the caller on the stack has AllPermission
     */
    private boolean validateBindAndUnbind() {
        return checkPerm(new AllPermission());
    }
    
    private boolean validateLookupAPI(ActiveDescriptor<?> candidate) {
        List<Permission> lookupPermissions = getLookupPermissions(candidate);
     
        for (Permission lookupPermission : lookupPermissions) {
            if (!checkPerm(lookupPermission)) {
                if (VERBOSE) {
                    System.out.println("candidate " + candidate +
                        " LOOKUP FAILED the security check for permission " + lookupPermission);
                }
                return false;
            }
        }
        
        return true;
        
    }
    
    private boolean validateInjection(ActiveDescriptor<?> candidate, Injectee injectee) {
        List<Permission> lookupPermissions = getLookupPermissions(candidate);
        
        // If this is an Inject, get the protection domain of the injectee
        final Class<?> injecteeClass = injectee.getInjecteeClass();
        
        ProtectionDomain pd = AccessController.doPrivileged(new PrivilegedAction<ProtectionDomain>() {

            @Override
            public ProtectionDomain run() {
                return injecteeClass.getProtectionDomain();
            }
            
        });
        
        for (Permission lookupPermission : lookupPermissions) {
            if (!pd.implies(lookupPermission)) {
                if (VERBOSE) {
                    System.out.println("candidate " + candidate + " injectee " + injectee +
                        " LOOKUP FAILED the security check for " + lookupPermission);
                }
                
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validates the lookup operation, with the two sub-cases of someone
     * calling the lookup API directly (injectee is null) or the lookup
     * occuring on behalf of an injection point (injectee is not null)
     * 
     * @param candidate The descriptor that would be used to create the object
     * to be injected or returned from the lookup
     * @param injectee If not null the injection point that will be injected into
     * @return true if the candidate can be looked up, false if the candidate should
     * not be available for lookup
     */
    private boolean validateLookup(ActiveDescriptor<?> candidate, Injectee injectee) {
        if (injectee == null) {
            return validateLookupAPI(candidate);
        }
        
        return validateInjection(candidate, injectee);
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Validator#validate(org.glassfish.hk2.api.Operation, org.glassfish.hk2.api.ActiveDescriptor, org.glassfish.hk2.api.Injectee)
     */
    @Override
    public boolean validate(ValidationInformation info) {
        switch(info.getOperation()) {
        case BIND:
        case UNBIND:
            return validateBindAndUnbind();
        case LOOKUP:
            return validateLookup(info.getCandidate(), info.getInjectee());
        default:
            return false;
        }
    }
    
    private static Permission getLookupPermission(String packName) {
        RuntimePermission retVal = new RuntimePermission(ACCESS_IN_PACKAGE + packName);
        
        return retVal;
    }
    
    private static List<Permission> getLookupPermissions(ActiveDescriptor<?> ad) {
        LinkedList<Permission> retVal = new LinkedList<Permission>();
        
        for (String contract : ad.getAdvertisedContracts()) {
            int index = contract.lastIndexOf('.');
            String packName = contract.substring(0, index);
            retVal.add(getLookupPermission(packName));
        }
        
        return retVal;
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
