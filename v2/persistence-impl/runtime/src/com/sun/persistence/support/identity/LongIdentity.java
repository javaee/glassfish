/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

/*
 * LongIdentity.java
 *
 */
 
package com.sun.persistence.support.identity;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/** This class is for identity with a single long field.
 * @version 2.0
 */
public class LongIdentity extends SingleFieldIdentity {
	
    /** The key.
     */
    private long key;

    /** Constructor with class and key.
     * @param pcClass the class
     * @param key the key
     */
    public LongIdentity (Class pcClass, long key) {
        super (pcClass);
        this.key = key;
        hashCode = hashClassName() ^ (int)key;
    }

    /** Constructor with class and key.
     * @param pcClass the class
     * @param key the key
     */
    public LongIdentity (Class pcClass, Long key) {
        this (pcClass, key.longValue ());
    }

    /** Constructor with class and key.
     * @param pcClass the class
     * @param str the key
     */
    public LongIdentity (Class pcClass, String str) {
        this (pcClass, Long.parseLong(str));
    }

    /** Constructor only for Externalizable.
     */
    public LongIdentity () {
    }

    /** Return the key.
     * @return the key
     */
    public long getKey () {
        return key;
    }

    /** Return the String form of the key.
     * @return the String form of the key
     */
    public String toString () {
        return Long.toString(key);
    }

    /** Determine if the other object represents the same object id.
     * @param obj the other object
     * @return true if both objects represent the same object id
     */
    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        } else if (!super.equals (obj)) {
            return false;
        } else {
            LongIdentity other = (LongIdentity) obj;
            return key == other.key;
        }
    }

    /** Write this object. Write the superclass first.
     * @param out the output
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal (out);
        out.writeLong(key);
    }

    /** Read this object. Read the superclass first.
     * @param in the input
     */
    public void readExternal(ObjectInput in)
		throws IOException, ClassNotFoundException {
        super.readExternal (in);
        key = in.readLong();
        hashCode = hashClassName() ^ (int)key;
    }
}
