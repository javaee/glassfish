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
package oracle.toplink.essentials.internal.ejb.cmp3.xml.listeners;

import java.lang.reflect.Method;

import oracle.toplink.essentials.internal.ejb.cmp3.metadata.listeners.MetadataEntityClassListener;

/**
 * An XML specified entity class event listener.
 * 
 * WIP - similar code in here as XMLEntityListener
 * 
 * @author Guy Pelletier
 * @since TopLink EJB 3.0 Reference Implementation
 */
public class XMLEntityClassListener extends MetadataEntityClassListener {
    /**
     * INTERNAL:
     */
    public XMLEntityClassListener(Class entityClass) {
        super(entityClass);
    }

    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPostBuildMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(POST_BUILD, method)) {
            super.setPostBuildMethod(method);
        }
    }
    
    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPostCloneMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(POST_CLONE, method)) {
            super.setPostCloneMethod(method);
        }
    }

    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPostDeleteMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(POST_DELETE, method)) {
            super.setPostDeleteMethod(method);
        }
    }

    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPostInsertMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(POST_INSERT, method)) {
            super.setPostInsertMethod(method);
        }
    }

    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPostRefreshMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(POST_REFRESH, method)) {
            super.setPostRefreshMethod(method);
        }
    }
    
    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPostUpdateMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(POST_UPDATE, method)) {
            super.setPostUpdateMethod(method);
        }
    }

    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPrePersistMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(PRE_PERSIST, method)) {
            super.setPrePersistMethod(method);
        }
    }
    
    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPreRemoveMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(PRE_REMOVE, method)) {
            super.setPreRemoveMethod(method);
        }
    }
    
    /**
     * INTERNAL:
     * Set it only if the same method wasn't already set from XML. If this
     * is a different method and one has already been set from XML, then an 
     * exception will be thrown from the set on the parent.
     */
    public void setPreUpdateWithChangesMethod(Method method) {
        if (noCallbackMethodAlreadySetFor(PRE_UPDATE_WITH_CHANGES, method)) {
            super.setPreUpdateWithChangesMethod(method);
        }
    }
}
