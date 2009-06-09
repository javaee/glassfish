package org.glassfish.enterprise.admin.ncli;

import org.glassfish.api.admin.cli.OptionType;
import org.glassfish.enterprise.admin.ncli.metadata.OptionDesc;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/** This class represents the option of a command. An option has a Name, Effective Type and Effective Value.
 *  All of the meta-data of the option is refactored into a generated class OptionDesc. For convenience,
 *  some of the methods of this class pass through to the given OptionDesc instance.
 *  <p>
 * Instances of this class are immutable. Arguably, they are mutable since the OperandDesc class which
 *  is a JAXB generated class is mutable. Callers are expected not to change the metadata given to this
 *  class after the instance of this class is created. Otherwise, bad things will happen.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net) (km@dev.java.net)
 * @see OptionDesc
 * @see OptionType

 */
final class Option {

    private final OptionDesc metadata;
    private final String givenValue;

    /** Constructs an Option with given meta-data and specified value. The specified value implies any value specified
     *  on the command line. A null specified value means that there was no value specified for this option on the
     *  command line, implying such an option would assume its default value if it has one.
     * @param metadata OptionDesc denoting the description of the option
     * @param givenValue String denoting its specified value; may be null
     */
    Option(OptionDesc metadata, String givenValue) {
        if (metadata == null)
            throw new IllegalArgumentException("null option metadata");
        this.metadata = metadata;
        if ("BOOLEAN".equals(metadata.getType())) {
            if(!("TRUE".equalsIgnoreCase(givenValue) || "FALSE".equalsIgnoreCase("FALSE")))
                throw new IllegalArgumentException("A boolean option has be specified as true or false only");
        }    
        this.givenValue = givenValue; //may be null, but is expected to be non-null
    }

    String getName() {
        return metadata.getName();
    }

    /** Returns the Effective Value of an option. Effective Value of an option is defined as
     *  the value processed after taking into account its Effective Type. Note that this method
     *  can still return a null.
     * @return String representing the effective value of this option.
     */
    String getEffectiveValue() {
        String value = (givenValue != null ? givenValue : metadata.getDefaultValue());
        if (value == null)
            return value;
        String type  = this.getEffectiveType();
        if(OptionType.FILE.name().equals(type) ||
           OptionType.DIRECTORY.name().equals(type) ||
           OptionType.FILE_PATH.name().equals(type))
            return new File(value).getAbsolutePath();
        if(OptionType.BOOLEAN.name().equals(type))
            return value.toLowerCase();
        return value;

    }

    /** Returns the type defined in the metadata if the overriding-type is absent. If the overriding-type
     *  is present, returns that type. Never returns a null.
     * @return String representing effective type of this option. Its value is that of name() called on
     * the correct OptionType.
     * @see OptionType
     */
    String getEffectiveType() {
        if (metadata.getOverridingType() == null)
            return metadata.getType();
        else
            return metadata.getOverridingType().getType();
    }
    String getDefaultValue() {
        return metadata.getDefaultValue();
    }

    boolean repeats() {
        String mr = metadata.getRepeats();
        return Boolean.valueOf(mr);
    }
    boolean required() {
        String mr = metadata.getRequired();
        return Boolean.valueOf(mr);
    }
    boolean hasSymbol() {
        String ms = metadata.getSymbol();
        return (ms != null);
    }
    char getSymbol() {
        String ms = metadata.getSymbol();
        if (ms == null)
            return '\u0000';
        assert ms.length() == 1 : "Symbol has to be a single ASCII character";
        return ms.charAt(0);
    }
    List<String> getLegalValues() {
        String lv = metadata.getLegalValues();
        if (lv == null)
            return Collections.emptyList();
        StringTokenizer st = new StringTokenizer(lv, ",");
        List<String> values = new ArrayList<String>();
        while(st.hasMoreTokens())
            values.add(st.nextToken());
        return values;
    }


    @Override
    public String toString() {
        return (this.getName() + ParseUtilities.OPTION_NAME_VALUE_SEPARATOR + this.getEffectiveValue());
    }
    
    static String toString(String name, String value) {
        if (name == null || value == null)
            throw new IllegalArgumentException("null arg");
        return (name + ParseUtilities.OPTION_NAME_VALUE_SEPARATOR + value);
    }

    static String toCommandLineOption(String s) {
        return "--" + s;
    }
    static String toCommandLineOption(char c) {
        return "-" + c;
    }
}
