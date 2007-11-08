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

import java.util.List;
import oracle.toplink.essentials.internal.sessions.ObjectChangeSet;
import oracle.toplink.essentials.internal.sessions.CollectionChangeRecord;

/**
 * <p><b>Purpose</b>: A ListContainerPolicy is ContainerPolicy whose container class
 * implements the List interface.  This signifies that the collection has order
 * <p>
 * <p><b>Responsibilities</b>:
 * Provide the functionality to operate on an instance of a List.
 *
 * @see ContainerPolicy
 * @see CollectionContainerPolicy
 */
public class ListContainerPolicy extends CollectionContainerPolicy {
    /**
     * INTERNAL:
     * Construct a new policy.
     */
    public ListContainerPolicy() {
        super();
    }

    /**
     * INTERNAL:
     * Construct a new policy for the specified class.
     */
    public ListContainerPolicy(Class containerClass) {
        super(containerClass);
    }
    
    /**
     * INTERNAL:
     * Construct a new policy for the specified class name.
     */
    public ListContainerPolicy(String containerClassName) {
        super(containerClassName);
    }

    /**
     * INTERNAL:
     * Returns true if the collection has order.
     */
    public boolean hasOrder() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Returns true if this is a ListContainerPolicy.
     */
    public boolean isListPolicy() {
        return true;
    }
    
    /**
     * INTERNAL:
     * Validate the container type.
     */
    public boolean isValidContainer(Object container) {
        // PERF: Use instanceof which is inlined, not isAssignable which is very inefficent.
        return container instanceof List;
    }

    /**
     * This method is used to bridge the behaviour between Attribute Change 
     * Tracking and deferred change tracking with respect to adding the same
     * instance multiple times. Each containerplicy type will implement specific 
     * behaviour for the collection type it is wrapping. These methods are only 
     * valid for collections containing object references.
     */
    public void recordAddToCollectionInChangeRecord(ObjectChangeSet changeSetToAdd, CollectionChangeRecord collectionChangeRecord){
        if (collectionChangeRecord.getRemoveObjectList().containsKey(changeSetToAdd)) {
            collectionChangeRecord.getRemoveObjectList().remove(changeSetToAdd);
        } else {
            if (collectionChangeRecord.getAddObjectList().contains(changeSetToAdd)) {
                collectionChangeRecord.getAddOverFlow().add(changeSetToAdd);
            } else {
                collectionChangeRecord.getAddObjectList().put(changeSetToAdd, changeSetToAdd);
            }
        }
    }
    
    /**
     * This method is used to bridge the behaviour between Attribute Change 
     * Tracking and deferred change tracking with respect to adding the same 
     * instance multiple times. Each container policy type will implement 
     * specific behaviour for the collection type it is wrapping. These methods 
     * are only valid for collections containing object references.
     */
    public void recordRemoveFromCollectionInChangeRecord(ObjectChangeSet changeSetToRemove, CollectionChangeRecord collectionChangeRecord){
        if (collectionChangeRecord.getAddObjectList().containsKey(changeSetToRemove)) {
            if (collectionChangeRecord.getAddOverFlow().contains(changeSetToRemove)){
                collectionChangeRecord.getAddOverFlow().remove(changeSetToRemove);
            } else {
                collectionChangeRecord.getAddObjectList().remove(changeSetToRemove);
            }
        } else {
            collectionChangeRecord.getRemoveObjectList().put(changeSetToRemove, changeSetToRemove);
        }
    }
}
