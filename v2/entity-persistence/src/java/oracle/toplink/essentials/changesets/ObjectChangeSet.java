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
package oracle.toplink.essentials.changesets;

import java.util.Vector;
import oracle.toplink.essentials.sessions.Session;

/**
 * <p>
 * <b>Purpose</b>: Provides API to the Class that holds all changes made to a particular Object.
 * <p>
 * <b>Description</b>: The ObjectChangeSet class represents a single Object registered in the UnitOfWork.
 * It is owned by the larger UnitOfWorkChangeSet.
 * <p>
 */
public interface ObjectChangeSet {
    boolean equals(ObjectChangeSet objectChange);

    /**
     * ADVANCED:
     * This method will return a collection of the fieldnames of attributes changed in an object.
     */
    Vector getChangedAttributeNames();

    /**
     * ADVANCED:
     * This method returns a reference to the collection of changes within this changeSet.
     */
    Vector getChanges();

    /**
     * ADVANCE:
     * This method returns the class type that this changeSet Represents.
     */
    Class getClassType(Session session);

    /**
     * ADVANCE:
     * This method returns the class Name that this changeSet Represents.
     */
    String getClassName();

    /**
     * ADVANCED:
     * This method returns the key value that this object was stored under in it's respective Map.
     * This is old relevant for collection mappings that use a Map.
     */
    Object getOldKey();

    /**
     * ADVANCED:
     * This method returns the key value that this object will be stored under in it's respective Map.
     * This is old relevant for collection mappings that use a Map.
     */
    Object getNewKey();

    /**
     * ADVANCED:
     * This method returns the primary keys for the object that this change set represents.
     */
    Vector getPrimaryKeys();

    /**
     * ADVANCED:
     * This method is used to return the parent ChangeSet.
     */
    UnitOfWorkChangeSet getUOWChangeSet();

    /**
     * ADVANCED:
     * This method is used to return the lock value of the object this changeSet represents.
     */
    Object getWriteLockValue();
    
    /**
     * ADVANCED:
     * Returns the change record for the specified attribute name.
     */
    ChangeRecord getChangesForAttributeNamed(String attributeName);
    
    /**
     * ADVANCED:
     * This method will return true if the specified attributue has been changed..
     * @param String the name of the attribute to search for.
     */
    boolean hasChangeFor(String attributeName);

    /**
     * ADVANCED:
     * Returns true if this particular changeSet has changes.
     */
    boolean hasChanges();

    /**
     * ADVANCED:
     * Returns true if this ObjectChangeSet represents a new object.
     */
    boolean isNew();
}
