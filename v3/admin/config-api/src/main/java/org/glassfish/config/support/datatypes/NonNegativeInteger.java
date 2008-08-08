/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.config.support.datatypes;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.DataType;
import org.jvnet.hk2.config.ValidationException;

/** Represents an integer from 0 to Integer.MAX_VALUE. 
 *  It's modeled as a functional class.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@Service
public class NonNegativeInteger implements DataType {

    /** Validates the value as a non-negative integer.
     * @param value
     * @throws org.jvnet.hk2.config.ValidationException
     */
    public void validate(String value) throws ValidationException {
        if (value == null)
            throw new ValidationException("null value is not of type NonNegativeInteger");
        if (isTokenized(value))
            return; //a token is always valid
        try {
            int number = Integer.parseInt(value);
            if (number < 0 || number > Integer.MAX_VALUE) //taken from ServerSocket.java
                throw new ValidationException("value: " + number + " not applicable for NonNegativeInteger [0, " + Integer.MAX_VALUE + "] data type");
        } catch(NumberFormatException e) {
            throw new ValidationException(e);
        }
    }
    /*package*/
    static boolean isTokenized(String value) {
        if (value != null && value.startsWith("{") && value.endsWith("}"))
            return true;
        return false;
    }
}
