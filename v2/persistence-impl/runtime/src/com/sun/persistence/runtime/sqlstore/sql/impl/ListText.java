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

package com.sun.persistence.runtime.sqlstore.sql.impl;

/**
 * A utility class that helps construct ',' separated list of text.
 * @author Mitesh Meswani
 */
public class ListText {
    private final static String COMMA = ", ";
    private StringBuffer text = new StringBuffer();

    /**
     * The no-arg constructor
     */
    public ListText () {
    }

    /**
     * Initialize the object with given str
     * @param str The initial text.
     */
    public ListText (String str) {
        append(str);
    }

    /**
     * Append the given str to the column text. Appends ',' to the existing column
     * text before appending the given str if its length is non zero.
     * @param str the text
     * @return A reference to this object
     */
    public ListText append(String str) {
        if ( text.length() > 0) {
            text.append(COMMA);
        }
        text.append(str);
        return this;
    }

    /**
     * Append the given str to the column text. Appends ',' to the existing column
     * text before appending the given str if its length is non zero.
     * @param str the text
     * @return A reference to this object
     */
    public ListText append(StringBuffer str) {
        if ( text.length() > 0) {
            text.append(COMMA);
        }
        text.append(str);
        return this;
    }

    /**
     * Append the given <code>listText</code> to this. Appends ',' to the
     * existing text before appending the given text if its length
     * is non zero.
     * @param listText the text
     * @return A reference to this object
     */
    public ListText append(ListText listText) {
        text.append(listText.text);
        return this;
    }

    /**
     * Returns a string representing the list text
     * @return A string representing the list text
     */
    public String toString() {
        return text.toString();
    }

    /**
     * Returns the StringBuffer backing this list text. Please note that changes
     * made to the backing buffer are directly reflected to this list text.
     * @return The StringBuffer backing this list text.
     */
    public StringBuffer getBackingStringBuffer() {
        return text;
    }


}
