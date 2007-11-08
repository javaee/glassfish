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
package oracle.toplink.essentials.sequencing;

import java.util.Vector;
import java.math.*;
import oracle.toplink.essentials.internal.sessions.AbstractSession;
import oracle.toplink.essentials.internal.databaseaccess.Accessor;
import oracle.toplink.essentials.exceptions.ValidationException;
import oracle.toplink.essentials.exceptions.DatabaseException;

/**
 * <p>
 * <b>Purpose</b>: An abstract class providing default sequence behavior.
 * <p>
 */
public abstract class StandardSequence extends Sequence {
    public StandardSequence() {
        super();
    }

    public StandardSequence(String name) {
        super(name);
    }

    public StandardSequence(String name, int size) {
        super(name, size);
    }

    public StandardSequence(String name, int size, int initialValue) {
        super(name, size, initialValue);
    }
    
    public void onConnect() {
        // does nothing
    }

    public void onDisconnect() {
        // does nothing
    }

    protected abstract Number updateAndSelectSequence(Accessor accessor, AbstractSession writeSession, String seqName, int size);

    public abstract boolean shouldAcquireValueAfterInsert();

    public abstract boolean shouldUseTransaction();

    public Object getGeneratedValue(Accessor accessor, AbstractSession writeSession, String seqName) {
        if (shouldUsePreallocation()) {
            return null;
        } else {
            Number value = updateAndSelectSequence(accessor, writeSession, seqName, 1);
            if (value == null) {
                throw DatabaseException.errorPreallocatingSequenceNumbers();
            }
            return value;
        }
    }

    public Vector getGeneratedVector(Accessor accessor, AbstractSession writeSession, String seqName, int size) {
        if (shouldUsePreallocation()) {
            Number value = updateAndSelectSequence(accessor, writeSession, seqName, size);
            if (value == null) {
                throw DatabaseException.errorPreallocatingSequenceNumbers();
            }
            return createVector(value, seqName, size);
        } else {
            return null;
        }
    }

    /**
    * INTERNAL:
    * Indicates whether existing attribute value should be overridden.
    * This method is called in case an attribute mapped to PK of sequencing-using
    * descriptor contains non-null value.
    * Default implementation is: Always override for "after insert" sequencing,
    * override non-positive Numbers for "before insert" sequencing.
    * @param seqName String is sequencing number field name
    * @param existingValue Object is a non-null value of PK-mapped attribute.
    */
    public boolean shouldOverrideExistingValue(String seqName, Object existingValue) {
        if (shouldAcquireValueAfterInsert()) {
            return true;
        } else {
            // Check if the stored number is greater than zero (a valid sequence number)
            if (existingValue instanceof BigDecimal) {
                if (((BigDecimal)existingValue).signum() <= 0) {
                    return true;
                }
            } else if (existingValue instanceof BigInteger) {
                if (((BigInteger)existingValue).signum() <= 0) {
                    return true;
                }
            }
            //CR#2645: Fix for String ClassCastException.  Now we don't assume
            //anything which is not a BigDecimal to be a Number.
            else if (existingValue instanceof Number && (((Number)existingValue).longValue() <= 0)) {
                return true;
            }

            return false;
        }
    }

    /**
    * INTERNAL:
    * given sequence = 10, size = 5 will create Vector (6,7,8,9,10)
    * @param seqName String is sequencing number field name
    * @param existingValue Object is a non-null value of PK-mapped attribute.
    * @param size int size of Vector to create.
    */
    protected Vector createVector(Number sequence, String seqName, int size) {
        BigDecimal nextSequence;
        BigDecimal increment = new BigDecimal(1);

        if (sequence instanceof BigDecimal) {
            nextSequence = (BigDecimal)sequence;
        } else {
            nextSequence = new BigDecimal(sequence.doubleValue());
        }

        Vector sequencesForName = new Vector(size);

        nextSequence = nextSequence.subtract(new BigDecimal(size));

        // Check for incorrect values return to validate that the sequence is setup correctly.
        // PRS 36451 intvalue would wrap
        if (nextSequence.doubleValue() < -1) {
            throw ValidationException.sequenceSetupIncorrectly(seqName);
        }

        for (int index = size; index > 0; index--) {
            nextSequence = nextSequence.add(increment);
            sequencesForName.addElement(nextSequence);
        }
        return sequencesForName;
    }

    public void setInitialValue(int initialValue) {
        // sequence value should be positive
        if(initialValue <= 0) {
            initialValue = 1;
        }
        super.setInitialValue(initialValue);
    }
}
