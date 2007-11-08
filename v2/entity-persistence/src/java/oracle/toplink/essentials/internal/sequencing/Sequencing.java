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

package oracle.toplink.essentials.internal.sequencing;


/**
 * <p>
 * <b>Purpose</b>: Define interface to use sequencing.
 * <p>
 * <b>Description</b>: This interface accessed through Session.getSequencing() method.
 * Used by TopLink internals to obtain sequencing values.
 * <p>
 * <b>Responsibilities</b>:
 * <ul>
 * <li> Provides sequencing objects and supporting APIs.
 * </ul>
 */
public interface Sequencing {
    // Possible return values for whenShouldAcquireValueForAll() method:
    // all classes should acquire sequencing value before insert;
    public static final int BEFORE_INSERT = -1;

    // some classes should acquire sequencing value before insert, some after;
    public static final int UNDEFINED = 0;

    // all classes should acquire sequencing value after insert;
    public static final int AFTER_INSERT = 1;

    /**
    * INTERNAL:
    * Indicates when sequencing value should be acqiured for all classes.
    * There are just three possible return values:
    * BEFORE_INSERT, UNDEFINED, AFTER_INSERT.
    * Used as a shortcut to avoid individual checks for each class:
    * shouldAcquireValueAfterInsert(Class cls).
    * Currently UNDEFINED only happens in a case of a SessionBroker:
    * session1 - BEFORE_INSERT, session2 - AFTER_INSERT
    */
    public int whenShouldAcquireValueForAll();

    /**
    * INTERNAL:
    * Indicates whether sequencing value should be acqiured
    * before or after INSERT
    */
    public boolean shouldAcquireValueAfterInsert(Class cls);

    /**
    * INTERNAL:
    * Indicates whether existing attribute value should be overridden.
    * This method is called in case an attribute mapped to PK of sequencing-using
    * descriptor contains non-null value.
    * @param seqName String is sequencing number field name
    * @param existingValue Object is a non-null value of PK-mapped attribute.
    */
    public boolean shouldOverrideExistingValue(Class cls, Object existingValue);

    /**
    * INTERNAL:
    * Return the newly-generated sequencing value.
    * @param cls Class for which the sequencing value is generated.
    */
    public Object getNextValue(Class cls);
}
