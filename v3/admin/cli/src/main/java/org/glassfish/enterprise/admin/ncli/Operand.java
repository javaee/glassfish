package org.glassfish.enterprise.admin.ncli;

import org.glassfish.api.admin.cli.OptionType;
import org.glassfish.enterprise.admin.ncli.metadata.OperandDesc;

import java.io.File;

/** Class that represents the operands of a command. An operand has a metadata and one or more values. If a command
 *  takes an operand, its name is never null. If a command does not take an operand, it's called operand-less command.
 *
 * <p>
 * This is a package private class.
 *  <p>
 *  Instances of this class are immutable. Arguably, they are mutable since the OperandDesc class which
 *  is a JAXB generated class is mutable. Callers are expected not to change the metadata given to this
 *  class after the instance of this class is created. Otherwise, bad things will happen.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see org.glassfish.api.admin.cli.OptionType
 * @see Option
 * @see org.glassfish.enterprise.admin.ncli.metadata.OperandDesc
 */
final class Operand {

    private final OperandDesc metadata;
    private final String[] givenValues;

    /** Constructs an Operand from given metadata and array of strings each of which represents an operand.
     *  Calling classes should ensure the given values are according to the metadata. For instance, there is
     *  no point in creating an operand with null <code>values</code> if the metadata says it is a required
     *  operand.
     *  <p>
     *  Note that all the operand (values) of a command are of the same <i> type</i> decided by
     *  the Effective Type of the operand.     * 
     *  @param metadata  OperandDesc instance the describes what the operand is all about, may not be null
     *  @param values  String representation of the operand values, may not be null
     */
    Operand(OperandDesc metadata, String[] values) {
        if (metadata == null || values == null)
            throw new IllegalArgumentException("null arg");
        for (String ov : values)
            if (ov == null)
                throw new IllegalArgumentException("null value specified for an operand");
        this.metadata    = metadata;
        this.givenValues = new String[values.length];
        System.arraycopy(values, 0, givenValues, 0, values.length);
        initializeEffectiveValues();
    }

    /** Returns the name of the operand. At the very least, it will be the the name of the field
     *  in a command implementation that is annotated with annotation @Operand. If a command does
     *  not take any operands, there will be no fields annotated with @Operand. Such a command is
     *  called operand-less command. Thus, if an operand is accepted by a command, its name will
     *  never be null. If an operand is not accepted by a command, there is no need to create instances
     *  of this class.
     *
     * @return String representing the name of an operand. May not be null.
     */
    String getName() {
        return metadata.getName();
    }

    /** Returns the effective type of this operand. Note that all operands have the same effective type.
     *
     * @return one of enumerated values from OptionType
     * @see OptionType
     */
    String getEffectiveType() {
        if (metadata.getOverridingType() == null)
            return metadata.getType();
        else
            return metadata.getOverridingType().getType();        
    }

    /** An operand takes one or more values by definition. It is useful to know about an operand its cardinality. This
     *  method provides that convenience. It's a syntax error if a command only takes one operand and if multiple operands
     *  are specified on the command line.
     *
     * @return true if this operand takes exactly one value, false otherwise
     */
    boolean hasOneValue() {
        String c = metadata.getCardinality();
        return "ONE".equals(c);
    }

    /** A convenience method to decide if an operand takes two or more values. It's a syntax error if a command
     *  takens two or more operands and only 0 or 1 operand are specified on the command line.
     *
     * @return true if this command takes two or more operands, false otherwise
     * @see #hasOneValue
     */
    boolean hasManyValues() {
        return !hasOneValue();
    }

    /** Returns a copy of effective values of this operand.
     *
     * @return a copy of this operand's given values
     */
    String[] getEffectiveValues() {
        String[] copy = new String[givenValues.length];
        System.arraycopy(givenValues, 0, copy, 0, givenValues.length);
        return copy;
    }

    // ALL Private ...
    
    private void initializeEffectiveValues() {
        String type  = this.getEffectiveType();
        if(OptionType.FILE.name().equals(type) ||
           OptionType.DIRECTORY.name().equals(type) ||
           OptionType.FILE_PATH.name().equals(type)) {
            int i = 0;
            for (String v : givenValues) {
                givenValues[i++] = new File(v).getAbsolutePath();
            }
        }
        if(OptionType.BOOLEAN.name().equals(type)) {
            int i = 0;
            for (String v : givenValues) {
                givenValues[i++] = v.toLowerCase();
            }
        }
    }
}
