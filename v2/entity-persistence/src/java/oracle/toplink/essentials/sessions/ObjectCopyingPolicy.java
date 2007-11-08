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
package oracle.toplink.essentials.sessions;

import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.internal.localization.*;

/**
 * <b>Purpose</b>: Define how an object is to be copied.<p>
 * <b>Description</b>: This is for usage with the object copying feature, not the unit of work.
 *                     This is useful for copying an entire object graph as part of the
 *                     host application's logic.<p>
 * <b>Responsibilities</b>:<ul>
 * <li> Inidcate through CASCADE levels the depth relationships will copied.
 * <li> Indicate if PK attributes should be copied with existing value or should be reset.
 * </ul>
 * @since TOPLink/Java 3.0
 * @see Session#copyObject(Object, ObjectCopyingPolicy)
 */
public class ObjectCopyingPolicy {
    protected boolean shouldResetPrimaryKey;
    protected oracle.toplink.essentials.internal.sessions.AbstractSession session;
    protected IdentityHashtable copies;

    /** Policy depth that determines how the copy will cascade to the object's
        related parts */
    protected int depth;

    /** Depth level indicating that NO relationships should be included in the copy.
        Relationships that are not copied will include the default value of the object's
        instantiation policy */
    public static final int NO_CASCADE = 1;

    /** Depth level indicating that only relationships with mapping indicated privately-
        owned should be copied */
    public static final int CASCADE_PRIVATE_PARTS = 2;

    /** Depth level indicating that all relationships with mappings should be used when
        building the copied object graph */
    public static final int CASCADE_ALL_PARTS = 3;

    /**
     * PUBLIC:
     * Return a new copying policy.
     * By default the policy cascades privately owned parts and nulls primary keys.
     */
    public ObjectCopyingPolicy() {
        this.shouldResetPrimaryKey = true;
        // 2612538 - the default size of IdentityHashtable (32) is appropriate
        this.copies = new IdentityHashtable();
        this.depth = CASCADE_PRIVATE_PARTS;
    }

    /**
     * PUBLIC:
     * Set if the copy should cascade all relationships when copying the object(s).
     */
    public void cascadeAllParts() {
        setDepth(CASCADE_ALL_PARTS);
    }

    /**
     * PUBLIC:
     * Set if the copy should cascade only those relationships that are configured
     * as privately-owned.
     */
    public void cascadePrivateParts() {
        setDepth(CASCADE_PRIVATE_PARTS);
    }

    /**
     * PUBLIC:
     * Set if the copy should not cascade relationships when copying the object(s)
     */
    public void dontCascade() {
        setDepth(NO_CASCADE);
    }

    /**
     * INTERNAL: Get the session.
     */
    public IdentityHashtable getCopies() {
        return copies;
    }

    /**
     * INTERNAL: Return the cascade depth.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * INTERNAL: Return the session.
     */
    public oracle.toplink.essentials.internal.sessions.AbstractSession getSession() {
        return session;
    }

    /**
     * INTERNAL: Set the copies.
     */
    public void setCopies(IdentityHashtable newCopies) {
        copies = newCopies;
    }

    /**
     * INTERNAL: Set the cascade depth.
     */
    public void setDepth(int newDepth) {
        depth = newDepth;
    }

    /**
     * INTERNAL: Set the session.
     */
    public void setSession(oracle.toplink.essentials.internal.sessions.AbstractSession newSession) {
        session = newSession;
    }

    /**
     * PUBLIC:
     * Set if the primary key should be reset to null.
     */
    public void setShouldResetPrimaryKey(boolean newShouldResetPrimaryKey) {
        shouldResetPrimaryKey = newShouldResetPrimaryKey;
    }

    /**
     * PUBLIC:
     * Return true if the policy has been configured to CASCADE_ALL_PARTS or CASCADE_PRIVATE_PARTS.
     */
    public boolean shouldCascade() {
        return getDepth() != NO_CASCADE;
    }

    /**
     * PUBLIC:
     * Return true if the policy should CASCADE_ALL_PARTS
     */
    public boolean shouldCascadeAllParts() {
        return getDepth() == CASCADE_ALL_PARTS;
    }

    /**
     * PUBLIC:
     * Return true if the policy should CASCADE_PRIVATE_PARTS
     */
    public boolean shouldCascadePrivateParts() {
        return getDepth() == CASCADE_PRIVATE_PARTS;
    }

    /**
     * PUBLIC:
     * Return if the primary key should be reset to null.
     */
    public boolean shouldResetPrimaryKey() {
        return shouldResetPrimaryKey;
    }

    /**
     * INTERNAL:
     */
    public String toString() {
        String depthString = "";
        if (shouldCascadeAllParts()) {
            depthString = "CASCADE_ALL_PARTS";
        } else if (shouldCascadePrivateParts()) {
            depthString = "CASCADE_PRIVATE_PARTS";
        } else {
            depthString = "NO_CASCADING";
        }
        Object[] args = { depthString, new Boolean(shouldResetPrimaryKey()) };
        return Helper.getShortClassName(this) + ToStringLocalization.buildMessage("depth_reset_key", args);
    }
}
