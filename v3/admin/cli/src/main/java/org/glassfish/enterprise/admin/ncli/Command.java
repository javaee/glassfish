package org.glassfish.enterprise.admin.ncli;

import org.glassfish.cli.metadata.CommandDesc;

import java.util.Collections;
import java.util.List;

/** Represents a command class. The goal of the command line parser is to build a command. If the command
 *  can not be built, it is a syntax error. A command comprises of a list of options and a list of operands.
 *
 * Instances of this class are immutable.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see Option
 * @see Operand
 */
final class Command {
    private final String name;
    private final List<Option> options;
    private final List<Operand> operands;

    Command(CommandDesc metadata, List<Option> options, List<Operand> operands) {
        if (metadata == null || options == null || operands == null)
            throw new IllegalArgumentException("null metadata, options or operands");
        this.name = metadata.getName();
        this.options = Collections.unmodifiableList(options);
        this.operands = Collections.unmodifiableList(operands);
    }

    String getName() {
        return name;
    }
    List<Option> getOptions() {
        return options;
    }

    List<Operand> getOperands() {
        return operands;
    }
    
    Option getOptionNamed(String name) {
        for(Option opt : options) {
            if (opt.getName().equals(name))
                return opt;
        }
        return null;
    }

    String getOptionValue(String optionName) {
        Option opt = this.getOptionNamed(optionName);
        if (opt != null)
            return opt.getName();
        return null;
    }

    boolean hasOptionNamed(String optionName) {
        return getOptionNamed(optionName) != null;
    }
}
