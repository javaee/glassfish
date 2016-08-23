/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2016 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.hk2.runlevel.internal;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.runlevel.ErrorInformation;

/**
 * @author jwells
 *
 */
public class ErrorInformationImpl implements ErrorInformation {
    private final Throwable error;
    private ErrorAction action;
    private final Descriptor descriptor;
    
    /* package */ ErrorInformationImpl(Throwable error, ErrorAction action, Descriptor descriptor) {
        this.error = error;
        this.action = action;
        this.descriptor = descriptor;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.ErrorInformation#getError()
     */
    @Override
    public Throwable getError() {
        return error;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.ErrorInformation#getAction()
     */
    @Override
    public ErrorAction getAction() {
        return action;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.runlevel.ErrorInformation#setAction(org.glassfish.hk2.runlevel.ErrorInformation.ErrorAction)
     */
    @Override
    public void setAction(ErrorAction action) {
        if (action == null) throw new IllegalArgumentException("action may not be null in setAction");
        
        this.action = action;
    }

    @Override
    public Descriptor getFailedDescriptor() {
        return descriptor;
    }
    
    @Override
    public String toString() {
        String descriptorString = (descriptor == null) ? "null" : descriptor.getImplementation();
        return "ErrorInformationImpl(" + action + "," + descriptorString + "," + System.identityHashCode(this) + ")";
    }

}
