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
 * This interface provides constants denoting the treatment of null values 
 * for persistent fields during storage in the data store.
 *
 * @author Michael Bouschen
 */
public class NullValueTreatment 
{
    /**
     * Constant representing converting a null value of a field of nullable type 
     * to the default value for the type in the datastore.
     */
    public static final int NONE = 0;

    /** 
     * Constant representing throwing an exception when storing a null value of 
     * field of a nullable type that is mapped to non-nullable type in the 
     * datastore.
     */
    public static final int EXCEPTION = 1;

    /**
     * Constant representing converting a null value of a field of nullable type 
     * to the default value for the type in the datastore.
     */
    public static final int DEFAULT = 2;

    /**
     * Returns a string representation of the specified NullValueTreatment 
     * constant.  
     * @param nullValueTreatment the null value treatment, one of 
     * {@link #NONE}, {@link #EXCEPTION} or {@link #DEFAULT}
     * @return the string representation of the NullValueTreatment constant
     */
    public static String toString(int nullValueTreatment) 
    {
        switch (nullValueTreatment) {
        case NONE :
            return "none"; //NOI18N
        case EXCEPTION :
            return "exception"; //NOI18N
        case DEFAULT :
            return "default"; //NOI18N
        default:
            return "UNSPECIFIED"; //NOI18N
        }
    }

    /**
     * Returns the NullValueTreatment constant for the string representation.
     * @param nullValueTreatment the string representation of the null value
     * treatment
     * @return the null value treatment, one of {@link #NONE}, 
     * {@link #EXCEPTION} or {@link #DEFAULT}
     **/
    public static int toNullValueTreatment(String nullValueTreatment)
    {
        if ((nullValueTreatment == null) || (nullValueTreatment.length() == 0))
            return NONE;
 
        if ("none".equals(nullValueTreatment)) //NOI18N
            return NONE;
        else if ("exception".equals(nullValueTreatment)) //NOI18N
            return EXCEPTION;
        else if ("default".equals(nullValueTreatment)) //NOI18N
            return DEFAULT;
        else
            return NONE;
    }
}
