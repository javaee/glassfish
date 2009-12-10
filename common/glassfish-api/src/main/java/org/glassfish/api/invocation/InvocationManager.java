/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.api.invocation;


import org.glassfish.api.invocation.ComponentInvocation.ComponentInvocationType;
import org.jvnet.hk2.annotations.Contract;

/**
 * InvocationManager provides interface to keep track of
 * component context on a per-thread basis
 */

@Contract
public interface InvocationManager {

    /**
     * To be called by a Container to indicate that the Container is
     * about to invoke a method on a component.
     * The preInvoke and postInvoke must be called in pairs and well-nested.
     *
     * @param inv the Invocation object
     */
    public <T extends ComponentInvocation> void preInvoke(T inv) throws InvocationException;

    /**
     * To be called by a Container to indicate that the Container has
     * just completed the invocation of a method on a component.
     * The preInvoke and postInvoke must be called in pairs and well-nested.
     *
     * @param inv the Invocation object
     */
    public <T extends ComponentInvocation> void postInvoke(T inv) throws InvocationException;

    /**
     * Returns the current Invocation object associated with the current thread
     */
    public <T extends ComponentInvocation> T getCurrentInvocation();

    /**
     * Returns the previous Invocation object associated with the current
     * thread.
     * Returns null if there is none. This is typically used when a component A
     * calls another component B within the same VM. In this case, it might be
     * necessary to obtain information related to both component A using
     * getPreviousInvocation() and B using getCurrentInvocation()
     */
    public <T extends ComponentInvocation> T getPreviousInvocation()
            throws InvocationException;

    /**
     * return true iff no invocations on the stack for this thread
     */
    public boolean isInvocationStackEmpty();

    public java.util.List<? extends ComponentInvocation> getAllInvocations();
   

    public void registerComponentInvocationHandler(ComponentInvocationType type, 
            RegisteredComponentInvocationHandler handler);
    


}
