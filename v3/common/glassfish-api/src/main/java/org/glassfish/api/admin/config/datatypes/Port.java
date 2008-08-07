/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.api.admin.config.datatypes;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.DataType;
import org.jvnet.hk2.config.ValidationException;

/** Represents a network port on a machine. It's modeled as a functional class.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@Service
public final class Port implements DataType {

    /** Checks if given string represents a port. Does not allow any value other
     *  than those between 0 and 65535 (both inclusive).
     * @param value represents the value of the port. 
     * @throws org.jvnet.hk2.config.ValidationException if the value does not represent
     * integer value.
     */
    public void validate(String value) throws ValidationException {
        try {
            int port = Integer.parseInt(value);
            if (port < 0 || port > 0xFFFF) //taken from ServerSocket.java
                throw new ValidationException("value: " + port + "not applicable for Port [0, " + 0xFFFF + "] data type");
        } catch(NumberFormatException e) {
            throw new ValidationException(e);
        }
    }
}