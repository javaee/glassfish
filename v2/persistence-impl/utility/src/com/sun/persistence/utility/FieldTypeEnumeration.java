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
 * FieldTypeEnumeration
 *
 * Created on January 31, 2003
 */


package com.sun.persistence.utility;

/**
 *
 */
public interface FieldTypeEnumeration {

    //Not Enumerated
    public static final int NOT_ENUMERATED = 0;

    //Primitive
    public static final int BOOLEAN_PRIMITIVE = 1;
    public static final int CHARACTER_PRIMITIVE = 2;
    public static final int BYTE_PRIMITIVE = 3;
    public static final int SHORT_PRIMITIVE = 4;
    public static final int INTEGER_PRIMITIVE = 5;
    public static final int LONG_PRIMITIVE = 6;
    public static final int FLOAT_PRIMITIVE = 7;
    public static final int DOUBLE_PRIMITIVE = 8;
    //Number
    public static final int BOOLEAN = 11;
    public static final int CHARACTER = 12;
    public static final int BYTE = 13;
    public static final int SHORT = 14;
    public static final int INTEGER = 15;
    public static final int LONG = 16;
    public static final int FLOAT = 17;
    public static final int DOUBLE = 18;
    public static final int BIGDECIMAL = 19;
    public static final int BIGINTEGER = 20;
    //String
    public static final int STRING = 21;
    //Dates
    public static final int UTIL_DATE = 22;
    public static final int SQL_DATE = 23;
    public static final int SQL_TIME = 24;
    public static final int SQL_TIMESTAMP = 25;
    //Arrays
    public static final int ARRAY_BYTE_PRIMITIVE = 51;

}
