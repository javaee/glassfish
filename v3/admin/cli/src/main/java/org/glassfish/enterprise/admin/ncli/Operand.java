package org.glassfish.enterprise.admin.ncli;

import org.glassfish.cli.metadata.OperandDesc;
import org.glassfish.api.admin.cli.OptionType;

import java.io.File;

/** Class that represents the operands of a command.
 *
 * Instances of this class are immutable. 
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see org.glassfish.api.admin.cli.OptionType
 * @see Option
 * @see org.glassfish.cli.metadata.OperandDesc
 */
final class Operand {

    private final OperandDesc metadata;
    private final String[] givenValues;

    /** Constructs an Operand from given metadata and array of strings each of which represents an operand.
     *  Calling classes should ensure the given values are according to the metadata. For instance, there is
     *  no point in creating an operand with null <code> values</code> if the metadata says it is a required
     *  operand.
     *  <p>
     *  Note that all the operand (values) of a command are of the same <i> type</i> decided by
     *  the Effective Type of the operand.
     *  <p>
     *  Instances of this class are immutable.
     *  @param metadata  OperandDesc instance the describes what the operand is all about
     *  @param values  String representation of the operand values
     */
    Operand(OperandDesc metadata, String[] values) {
        this.metadata      = metadata;
        this.givenValues   = values;
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

    String getEffectiveType() {
        if (metadata.getOverridingType() == null)
            return metadata.getType();
        else
            return metadata.getOverridingType().getType();        
    }
    boolean hasOneValue() {
        String c = metadata.getCardinality();
        return "ONE".equals(c);
    }
    
    boolean hasManyValues() {
        return !hasOneValue();
    }

    String[] getEffectiveValues() {
        String type  = this.getEffectiveType();
        if(OptionType.FILE.name().equals(type) ||
           OptionType.DIRECTORY.name().equals(type) ||
           OptionType.FILE_PATH.name().equals(type)) {
            String[] eff = new String[givenValues.length];
            int i = 0;
            for (String v:givenValues) {
                eff[i++] = new File(v).getAbsolutePath();
            }
            return eff;
        }

        if(OptionType.BOOLEAN.name().equals(type)) {
            String[] eff = new String[givenValues.length];
            int i = 0;
            for (String v:givenValues) {
                eff[i++] = v.toLowerCase();
            }
            return eff;
        }
        return givenValues;
    }
}
