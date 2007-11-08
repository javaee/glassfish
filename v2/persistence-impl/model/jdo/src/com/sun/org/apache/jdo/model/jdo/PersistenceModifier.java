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

package com.sun.org.apache.jdo.model.jdo;

/**
 * This interface provides constants denoting JDO specific 
 * modifiers for fields of a persistence-capable class.
 *
 * @author Michael Bouschen
 */
public class PersistenceModifier 
{
    /** Constant representing an unspecified field modifier */
    public static final int UNSPECIFIED = 0;

    /** Constant representing a none field modifier.  */
    public static final int NONE = 1;

    /** Constant representing a transactional field modifier. */
    public static final int TRANSACTIONAL = 2;

    /** Constant representing a persistence field modifier. */
    public static final int PERSISTENT  = 4;

    /** Constant representing a possibly persistence field modifier. */
    public static final int POSSIBLY_PERSISTENT  = 8;

    /**
     * Returns a string representation of the specified persistence modifer. 
     * @param persistenceModifier the persistence modifer, one of  
     * {@link #UNSPECIFIED}, {@link #NONE}, {@link #PERSISTENT},
     * {@link #TRANSACTIONAL}, or {@link #POSSIBLY_PERSISTENT}.
     * @return the string representation of the PersistenceModifer constant
     */
    public static String toString(int persistenceModifier) 
    {
        switch (persistenceModifier) {
        case NONE :
            return "none"; //NOI18N
        case TRANSACTIONAL :
            return "transactional"; //NOI18N
        case PERSISTENT:
            return "persistent"; //NOI18N
        case POSSIBLY_PERSISTENT:
            return "possibly-persistent"; //NOI18N
        default:
            return "UNSPECIFIED"; //NOI18N
        }
    }
    
    /**
     * Returns the PersistenceModifier constant for the specified string.
     * @param persistenceModifier the string representation of the persistence 
     * modifer
     * @return the persistence modifer, one of {@link #UNSPECIFIED}, 
     * {@link #NONE}, {@link #PERSISTENT} or {@link #TRANSACTIONAL}
     **/
    public static int toPersistenceModifier(String persistenceModifier)
    {
        if ((persistenceModifier == null) || (persistenceModifier.length() == 0))
            return UNSPECIFIED;
 
        if ("none".equals(persistenceModifier)) //NOI18N
            return NONE;
        else if ("transactional".equals(persistenceModifier)) //NOI18N
            return TRANSACTIONAL;
        else if ("persistent".equals(persistenceModifier)) //NOI18N
            return PERSISTENT;
        else if ("possibly-persistent".equals(persistenceModifier)) //NOI18N
            return POSSIBLY_PERSISTENT;
        else
            return UNSPECIFIED;
    }
}
