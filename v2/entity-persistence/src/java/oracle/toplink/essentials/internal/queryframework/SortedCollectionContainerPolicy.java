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
package oracle.toplink.essentials.internal.queryframework;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Comparator;
import java.lang.reflect.Constructor;
import oracle.toplink.essentials.exceptions.*;
import oracle.toplink.essentials.internal.security.PrivilegedAccessHelper;
import oracle.toplink.essentials.internal.security.PrivilegedGetConstructorFor;
import oracle.toplink.essentials.internal.security.PrivilegedInvokeConstructor;
import oracle.toplink.essentials.internal.security.PrivilegedNewInstanceFromClass;

/**
 * <p><b>Purpose</b>: A SortedCollectionContainerPolicy is ContainerPolicy whose
 * container class implements the SortedInterface interface.
 * Added for BUG # 3233263
 * <p>
 * <p><b>Responsibilities</b>:
 * Provide the functionality to operate on an instance of a SortedSet.
 *
 * @see ContainerPolicy
 * @see MapContainerPolicy
 */
public class SortedCollectionContainerPolicy extends CollectionContainerPolicy {
    protected Comparator m_comparator = null;

    /**
     * INTERNAL:
     * Construct a new policy.
     */
    public SortedCollectionContainerPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class.
     */
    public SortedCollectionContainerPolicy(Class containerClass) {
        super(containerClass);
    }

    /**
     * INTERNAL:
     * Sets a comparator object for this policy to use when instantiating
     * a new SortedSet object.
     */
    public void setComparator(Comparator comparator) {
        m_comparator = comparator;
    }

    /**
     * INTERNAL:
     * Return the stored comparator
     */
    public Comparator getComparator() {
        return m_comparator;
    }

    /**
     * INTERNAL
     * Override from ContainerPolicy. Need to maintain the comparator in the
     * new instance
     */
    public Object containerInstance() {
        try {
            if (m_comparator != null) {
                Object[] arguments = new Object[] { m_comparator };
                Class[] constructClass = new Class[] { Comparator.class };
                Constructor constructor = null;
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        constructor = (Constructor)AccessController.doPrivileged(new PrivilegedGetConstructorFor(getContainerClass(), constructClass, false));
                        return AccessController.doPrivileged(new PrivilegedInvokeConstructor(constructor, arguments));
                    } catch (PrivilegedActionException exception) {
                        throw QueryException.couldNotInstantiateContainerClass(getContainerClass(), exception.getException());
                    }
                } else {
                    constructor = PrivilegedAccessHelper.getConstructorFor(getContainerClass(), constructClass, false);
                    return PrivilegedAccessHelper.invokeConstructor(constructor, arguments);
                }
            } else {
                if (PrivilegedAccessHelper.shouldUsePrivilegedAccess()){
                    try {
                        return AccessController.doPrivileged(new PrivilegedNewInstanceFromClass(getContainerClass()));
                    } catch (PrivilegedActionException exception) {
                        throw QueryException.couldNotInstantiateContainerClass(getContainerClass(), exception.getException());
                    }
                } else {
                    return PrivilegedAccessHelper.newInstanceFromClass(getContainerClass());
                }
            }
        } catch (Exception ex) {
            throw QueryException.couldNotInstantiateContainerClass(getContainerClass(), ex);
        }
    }
}
