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

import java.util.Hashtable;
import java.util.Vector;

/**
 * <p>
 * <b>Purpose</b>: Provide public API to the OrderedCollectionChangeRecord.
 * <p>
 * <b>Description</b>: OrderedCollections, used in TopLink SDK, must be tracked differently from regulat Collections.
 * As the objects in the collection have a particular index which must be stored. This class stores the objects which must be written
 * into the collection and the indexes they must be written in at.  Inserting a new element at the beginning of the list will result
 * in the intire list being stored in the change set as the index of all other objects has changed.  Everything after the remove index will
 * be remove.
 */
public interface OrderedCollectionChangeRecord extends ChangeRecord {

    /**
     * ADVANCED:
     * This method returns the collection of indexes in which changes were made to this collection.
     * @return java.util.Vector
     */
    public Vector getAddIndexes();

    /**
     * ADVANCED:
     * This method returns the collection of ChangeSets that were added to the collection.
     * The indexes of these objects are the Keys of the Hashtable
     * @return java.util.Hashtable
     */
    public Hashtable getAddObjectList();

    /**
     * ADVANCED:
     * This method returns the index from where objects must be removed from the collection
     * @return int
     */
    public int getStartRemoveIndex();
}
