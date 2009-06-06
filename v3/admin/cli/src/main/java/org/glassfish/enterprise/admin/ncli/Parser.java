package org.glassfish.enterprise.admin.ncli;

import org.glassfish.cli.metadata.CommandDesc;
import org.glassfish.cli.metadata.OptionDesc;

import java.util.*;
import java.io.PrintStream;

import static org.glassfish.enterprise.admin.ncli.ParseUtilities.*;
import static org.glassfish.enterprise.admin.ncli.ParseUtilities.getOptionSymbolFromShortOption;
import static org.glassfish.enterprise.admin.ncli.ParseUtilities.hasOptionNameAndValue;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
/**
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
final class Parser {
    private static final ProgramOptionBuilder POB = ProgramOptionBuilder.getInstance();
    final Set<String> slc  = new HashSet<String>();
    final Set<String> uslc = new HashSet<String>();

    private final String[] args;
    private final PrintStream out;
    private final PrintStream err;
    
    private static final LocalStringsImpl lsm = new LocalStringsImpl(Parser.class);

    Parser(String[] args, PrintStream out, PrintStream err) {
        if (args == null || out == null)
            throw new IllegalArgumentException("null arg");
        this.args = args;
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
            String[] cmdArgs = splitUsingMetadata(argsToParse, known, givenProgramOptions);  //may contains options and operands
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
                            putHandlingRepeats(od, optionMap, "true");
                            si++;
                        } else { // not a boolean option or a boolean option specified as --name true/false
                            if (si == (argsToParse.length-1))
                                throw new ParserException(lsm.get("option.needs.value.symbol", Character.toString(c)));
                            OptionDesc lod = getOptionDescForSymbol(c, known);
                            if(lod != null) {
                                putHandlingRepeats(lod, optionMap, argsToParse[si+1]);
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
                            putHandlingRepeats(od, optionMap, "true");
                            si++;
                        } else { // not a boolean option or a boolean option specified as --name true/false
                            if (si == (argsToParse.length-1))
                                throw new ParserException(lsm.get("option.needs.value.name", name));
                            OptionDesc lod = getOptionDescForName(name, known);
                            if(lod != null) {
                                putHandlingRepeats(lod, optionMap, argsToParse[si+1]);
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
        String name = null;
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
            putHandlingRepeats(pod, filtrate, value); //this is a valid program option
        } else {
            unknown.add(argument);
        }
    }

    private static void handleNegativeLongBooleanOption(Set<OptionDesc> known, String argument, Map<String, String> filtrate, List<String> unknown) throws ParserException {
        String name = getOptionNameFromLongOption(argument);
        String value = "false";
        OptionDesc od = getOptionDescForName(name, known);
        if (od != null) {
            putHandlingRepeats(od, filtrate, value);
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
                putHandlingRepeats(od, filtrate, "true");
            }
        }

    }
    private static void putHandlingRepeats(OptionDesc od, Map<String, String> filtrate, String value) throws ParserException {
        if (!Boolean.valueOf(od.getRepeats().toLowerCase())) {  //repeats not allowed
            String name = od.getName();
            if (filtrate.containsKey(name)) {
                throw new ParserException(lsm.get("repeats.not.allowed", name));
            }
        }
        filtrate.put(od.getName(), value);
    }
}

