package org.glassfish.enterprise.admin.ncli;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import org.glassfish.enterprise.admin.ncli.metadata.CommandDesc;
import org.glassfish.enterprise.admin.ncli.metadata.OptionDesc;
import static org.glassfish.enterprise.admin.ncli.ParseUtilities.*;

import java.io.PrintStream;
import java.util.*;

/** An Ad-hoc parser that does at least two passes of the given command line (as an array of string arguments). Here are
 *  the goals of the Parser:
 * <ul>
 *   <li> Perform the first pass based on known asadmin program options. See {@link ProgramOptionBuilder}. The result of
 *        this pass is a syntax error as far as program options and their values are concerned, identification of a
 *        a server that possibly implements that command, and command's arguments as a list of strings. It the command's
 *        argument is an option, it will always be in the form "name=value" as a single string. The operands will
 *        be available verbatim. A successful first pass means no syntax error as far as the program options and
 *        command name is concerned. Anything that should be parsed for errors is put into the array of command arguments.
 *   </li>
 *   <li> Perform the second pass based on results of the first pass. Thus, knowing the command metadata (gotten from
 *        either running server or from a command metadata cache) and command arguments, another pass of command
 *        argument array is done. Any parsing errors are then reported as syntax errors. </li>
 *   <li> Both passes throw ParserException in case of syntax errors. </li>
 * </ul>
 *  The grammar for CLIP-compliant command line is rather complex and not fully specified in some cases. It's also not
 *  clear what happened to the CLIP OpenSolaris case, although a CLIP companion is available 
 *  <a href="http://arc.opensolaris.org/caselog/PSARC/2006/062/spec.opensolaris.clip.html"> here </a>. The parser also
 *  supports the old syntax (asadmin program options intermixed with command options) for compatibility
 *  new syntax (work TODO).
 *
 *  All instances of this class are immutable.
 *
 * Note that the name of a command is treated specially. Since not all legacy commands are implemented for GlassFish v3
 * and the legacy commands that suuport the old syntax, we need this kind of distinction.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @see ProgramOptionBuilder
 */

final class Parser {
    private static final ProgramOptionBuilder POB = ProgramOptionBuilder.getInstance();
    private final Set<String> slc  = new HashSet<String>();
    private final Set<String> uslc = new HashSet<String>();

    private final String[] args;
    private final PrintStream out;
    private final PrintStream err;
    
    private static final LocalStringsImpl lsm = new LocalStringsImpl(Parser.class);

    Parser(String[] args, PrintStream out, PrintStream err) {
        if (args == null || out == null)
            throw new IllegalArgumentException("null arg");
        this.args = new String[args.length];
        System.arraycopy(args, 0, this.args, 0, args.length);
        this.out  = out;
        this.err  = err;
        initialize();
    }
    Parser(String[] args) {
        this(args, System.out, System.err);
    }
    FirstPassResult firstPass() throws ParserException {
        String cmd;
        FirstPassResult fpr;
        if (indicatesCommandName(args[0])) {
            cmd = args[0];
            handleUnsupportedLegacyCommand(cmd);
            Set<OptionDesc> known = POB.getAllOptionMetadata();
            Map<String, String> givenProgramOptions = new HashMap<String, String>(); //given options as name value pairs
            int length = args.length-1;
            String[] argsToParse = new String[length];
            System.arraycopy(args, 1, argsToParse, 0, length);
            String[] cmdArgs = splitUsingMetadata(argsToParse, known, givenProgramOptions);  //may contain options and operands
            fpr = new FirstPassResult(cmd, givenProgramOptions, cmdArgs);
            return fpr;
        } else if (indicatesOption(args[0])) {
            //depends on whether we want to support the program option and command option separation
            throw new ParserException("As of now, command line must start with command name, not any of asadmin program options");
        } else {
            throw new ParserException(lsm.get("parser.invalid.start", args[0]));
        }
    }


    SecondPassResult secondPass(CommandDesc desc, String[] commandArguments) throws ParserException {
        //TODO
        return null;
    }

    // ALL Private ...
    // Private instance methods

    private void initialize() {
        initializeLegacyCommands();
    }

    private void initializeLegacyCommands() {
        ParseUtilities.file2Set(Constants.SUPPORTED_CMD_FILE_NAME, slc);
        ParseUtilities.file2Set(Constants.UNSUPPORTED_CMD_FILE_NAME, uslc);
    }

    private void handleUnsupportedLegacyCommand(String cmd) throws ParserException {
        for (String c : uslc) {
            if (c.equals(cmd)) {
                throw new ParserException(lsm.get("unsupported.legacy.command", cmd));
            }
        }
        //it is a supported command; do nothing
    }
    
    private static String[] splitUsingMetadata(String[] argsToParse, Set<OptionDesc> known, Map<String, String> optionMap) throws ParserException {
        //operates on argsToParse and splits it into program options and command options + operands based on given metadata (known)
        int si = 0;
        /* implementation note: watch how si is incremented! */
        List<String> remainingArgs = new ArrayList<String>();
        while (si < argsToParse.length) {
            String argument = argsToParse[si];
            if (indicatesOption(argument)) {
                if (indicatesNegativeLongBooleanOption(argument)) {
                    handleNegativeLongBooleanOption(known, argument, optionMap, remainingArgs);
                    si++;
                } else if (indicatesShortOption(argument)) {
                    if (hasOptionNameAndValue(argument)) {   //name and value specified as a single argument with =
                        handleOptionGivenNameValue(known, argument, optionMap, remainingArgs);
                        si++;
                    } else {  // it's a boolean option or an option followed by value as a separate argument, if non boolean
                        char c = getOptionSymbolFromShortOption(argument);
                        OptionDesc od = getOptionDescForBooleanOptionForSymbol(c, known);
                        if (od != null) { //it's boolean option
                            putHandlingConstraints(od, optionMap, "true");
                            si++;
                        } else { // not a boolean option or a boolean option specified as --name true/false
                            if (si == (argsToParse.length-1))
                                throw new ParserException(lsm.get("option.needs.value.symbol", Character.toString(c)));
                            OptionDesc lod = getOptionDescForSymbol(c, known);
                            if(lod != null) {
                                putHandlingConstraints(lod, optionMap, argsToParse[si+1]);
                                si += 2;
                            } else {
                                throw new ParserException(lsm.get("current.limitation.symbol", Character.toString(c)));
                            }
                        }
                    }
                } else {
                    assert indicatesLongOption(argument) : "Programming Error: option should be a long option: " + argument;
                    if (hasOptionNameAndValue(argument)) {
                        handleOptionGivenNameValue(known, argument, optionMap, remainingArgs);
                        si++;
                    }  else {
                        String name = getOptionNameFromLongOption(argument);
                        OptionDesc od = getOptionDescForBooleanOptionForName(name, known);
                        if (od != null) { //it's boolean option specified as --name
                            putHandlingConstraints(od, optionMap, "true");
                            si++;
                        } else { // not a boolean option or a boolean option specified as --name true/false
                            if (si == (argsToParse.length-1))
                                throw new ParserException(lsm.get("option.needs.value.name", name));
                            OptionDesc lod = getOptionDescForName(name, known);
                            if(lod != null) {
                                putHandlingConstraints(lod, optionMap, argsToParse[si+1]);
                                si += 2;
                            } else {
                                throw new ParserException(lsm.get("current.limitation.name", name));
                            }
                        }
                    }
                }
            } else if (indicatesBooleanOptionList(argument)) { //all boolean options combined
                handleBooleanOptionList(known, argument, optionMap, remainingArgs);
                si++;
            } else if (indicatesEndOfOptions(argument)) {
                //command line ran out of options ;) return the arguments after "--" verbatim *and stop*
                fillOperandsFromArgs(si+1, remainingArgs, argsToParse);
                break;
            } else {
                //throw new ParserException(lsm.get("invalid.argument.on.command.line", argument));
                //this is an operand!
                remainingArgs.add(argument);
                si++;
            }
        }
        return remainingArgs.toArray(new String[remainingArgs.size()]);
    }

    private static void handleOptionGivenNameValue(Set<OptionDesc> known, String argument, Map<String, String> filtrate, List<String> unknown) throws ParserException {
        String name;
        OptionDesc pod;
        if (indicatesShortOption(argument)) {
            char symbol = getOptionSymbolFromShortOption(argument);
            pod  = getOptionDescForSymbol(symbol, known);
            if (pod != null)
                name = pod.getName();
            else
                name = Character.toString(symbol);
        } else {
            //it has to indicate long option
            name = getOptionNameFromLongOption(argument);
            pod =  getOptionDescForName(name, known);
        }
        String value = getOptionValue(argument);
        if (pod != null) {
            if (!nonNullValueValidFor(pod, value)) {  // name exists, but value is invalid!
                throw new ParserException(lsm.get("invalid.value.for.known.option", name, value));
            }
            putHandlingConstraints(pod, filtrate, value); //this is a valid program option
        } else {
            unknown.add(argument);
        }
    }

    private static void handleNegativeLongBooleanOption(Set<OptionDesc> known, String argument, Map<String, String> filtrate, List<String> unknown) throws ParserException {
        String name = getOptionNameFromLongOption(argument);
        String value = "false";
        OptionDesc od = getOptionDescForName(name, known);
        if (od != null) {
            putHandlingConstraints(od, filtrate, value);
        } else {
            unknown.add(Option.toString(name, value));
        }
    }

    private static void fillOperandsFromArgs(int si, List<String> unknown, String[] from) {
        while (si < from.length) {
            unknown.add(from[si]);
            si++;
        }
    }
    private static void handleBooleanOptionList(Set<OptionDesc> known, String argument, Map<String, String> filtrate, List<String> unknown) throws ParserException {
        Map<Character, String> symbol2Name = getSymbolToNameMap(known);        
        Map<Character, String> trueOptions = new HashMap<Character, String>();
        booleanOptionListToOptionMap(argument, trueOptions);
        Set<Character> symbols = trueOptions.keySet();
        for (char c : symbols) {
            String optName = symbol2Name.get(c);
            if (optName == null) { // this is a possible symbol for command option
                unknown.add(Option.toString(optName, "true"));
            } else { // this is definitely a program option
                OptionDesc od = getOptionDescForName(optName, known);
                putHandlingConstraints(od, filtrate, "true");
            }
        }

    }
    private static void putHandlingConstraints(OptionDesc od, Map<String, String> filtrate, String value) throws ParserException {
        if (!Boolean.valueOf(od.getRepeats().toLowerCase())) {  //repeats not allowed
            String name = od.getName();
            if (filtrate.containsKey(name)) {
                throw new ParserException(lsm.get("repeats.not.allowed", name));
            }
        }
        if (isPassword(od))
            throw new ParserException(lsm.get("password.not.allowed.on.command.line", od.getName()));
        filtrate.put(od.getName(), value);
    }
}

