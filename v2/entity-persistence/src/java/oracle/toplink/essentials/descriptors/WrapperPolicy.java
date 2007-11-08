/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package oracle.toplink.essentials.descriptors;

import java.io.Serializable;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;

/**
 * <p><b>Purpose</b>: The wrapper policy can be used to wrap all objects read from the database in another object.
 * This allows for TopLink to utilize one version of the class for its purposes and allows for the
 * application to deal with another version of the object.
 * The wrapper policy is used for things such as EJB Entity Beans and is directly used by the
 * TopLink for WebLogic product for EJB Container Managed Persistence.
 *
 * It is assumed that relationships must be through the wrapper objects.
 * Object identity is not maintained on the wrapper objects, only the wrapped object.
 */
public interface WrapperPolicy extends Serializable {

    /**
     * PUBLIC:
     * Required: Lets the policy perform initialization.
     * @param session the session to initialize against
     */
    void initialize(AbstractSession session) throws DescriptorException;

    /**
     * PUBLIC:
     * Required: Return true if the wrapped value should be traversed.
     * Normally the wrapped value is looked after independently, it is not required to be traversed.
     */
    boolean isTraversable();

    /**
     * PUBLIC:
     * Required: Return true if the object is already wrapped.
     */
    boolean isWrapped(Object object);

    /**
     * PUBLIC:
     * Required: Set the descriptor.
     * @param descriptor the descriptor for the object being wrapped
     */
    void setDescriptor(ClassDescriptor descriptor);

    /**
     * PUBLIC:
     * Required: Unwrap the object to return the implementation that is meant to be used by TopLink.
     * The object may already be unwrapped in which case the object should be returned.
     * @param proxy the wrapped object
     * @param session the session to unwrap into
     */
    Object unwrapObject(Object proxy, AbstractSession session);

    /**
     * PUBLIC:
     * Required: Wrap the object to return the implementation that the application requires.
     * The object may already be wrapped in which case the object should be returned.
     * @param original, the object to be wrapped
     * @param session the session to wrap the object against.
     * @return java.lang.Object the wrapped object
     */
    Object wrapObject(Object original, AbstractSession session);
}
