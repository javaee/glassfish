/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jvnet.hk2.config;

/** Represents a Java primitive (and its wrapper) data type. Not all Java primitives
 *  are relevant from a configuration standpoint.
 * @see DataType
 * @see WriteableView#PRIMS
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since hk2 0.3.10
 */
public final class PrimitiveDataType extends DataType {

    private final String realType;
    PrimitiveDataType(String realType) {
        assert WriteableView.PRIMS.contains(realType) : "This class can't validate: " + realType;
        this.realType = realType;
    }
    
    @Override
    public void validate(String value) throws ValidationException {
        if (value.startsWith("${") && value.endsWith("}")) //it's a token
          return;
        boolean match = false;
        if ("int".equals(realType) || "java.lang.Integer".equals(realType))
            match = representsInteger(value);
        else if ("boolean".equals(realType) || "java.lang.Boolean".endsWith(realType))
            match = representsBoolean(value);
        else if ("char".equals(realType) || "java.lang.Character".equals(realType))
            representsChar(value);
        //no need for else as we are asserting it in the constructor
        if (!match) {
            String msg = "This value: " + value + " is not of type: " + realType + ", validation failed";
            throw new ValidationException(msg);            
        }
    }
    
    private static boolean representsBoolean(String value) {
        boolean isBoolean = "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
        return (isBoolean);
    }
    private static boolean representsChar(String value) {
            if (value.length() == 1)
                return true;
            return false;
    }
    private static boolean representsInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch(NumberFormatException ne) {
            return false;
        }
    }

}
