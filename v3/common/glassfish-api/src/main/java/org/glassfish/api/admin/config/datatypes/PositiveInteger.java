/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.api.admin.config.datatypes;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.DataType;
import org.jvnet.hk2.config.ValidationException;

/** Represents an integer from 1 to Integer.MAX_VALUE. 
 *  It's modeled as a functional class.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net) 
 */
@Service
public class PositiveInteger implements DataType { //could extend NonNegativeInteger
    
    /** Validates the given value as a positive integer.
     * @param value
     * @throws org.jvnet.hk2.config.ValidationException
     */
    public void validate(String value) throws ValidationException {
        try {
            int number = Integer.parseInt(value);
            if (number < 1 || number > Integer.MAX_VALUE) //taken from ServerSocket.java
                throw new ValidationException("value: " + number + "not applicable for PositiveInteger [1, " + Integer.MAX_VALUE + "] data type");
        } catch(NumberFormatException e) {
            throw new ValidationException(e);
        }
    }
}