package org.glassfish.enterprise.admin.ncli;

import org.glassfish.api.admin.cli.OptionType;
import org.glassfish.enterprise.admin.ncli.metadata.OptionDesc;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Provides utility routines to parse the command line. The regular expressions defined in this class as
 *  literal strings are the basis of command line parsing.  The class is composed of static methods.
 *  <p>
 *  As of now, this class is package-private.
 * 
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
final class ParseUtilities {

    /** A prefix that denotes "false" value of a boolean option specified as long name.
     *  This is <b> NOT</b> a regular expression.
     */
    static final String NEG_LONG_PREFIX                  = "--no-";

    /** A <b> regex </b> for the name of a command. Put literally, a command name contains one or more characters, with
     * first character being a letter (a-z or A-Z) and subsequent characters being letters, digits or hyphens or underscores.
     *
     */
    static final String CMD_NAME_REGEX                   = "^[a-zA-Z]([-_\\w]*)?$";

    /** A <b> regex</b> for a boolean short option (aka symbol) which can be specified as: <p>
     *  -c, or -c=true or -c=false, where c is exactly one letter between [a-z] or [A-Z].
     *
     */
    static final String SHORT_BOOLEAN_OPTION_REGEX       = "^-[a-zA-Z](=([t][r][u][e]|[f][a][l][s][e]))?$";

    /** A <b> regex </b> for a list of boolean options combined. Put literally, this is a string
     * starting with a hyphen followed by 2 or more chracters in the range [a-z] or [A-Z] and nothing else.
     *
     */
    static final String SHORT_BOOLEAN_OPTION_LIST_REGEX  = "^-[a-zA-Z][a-zA-Z]+$";

    /** A <b> regex </b> for a boolean option with a long name (rather than a symbol). Put literally, it is two hyphens followed
     *  by 2 or more characters with first character in the range [a-z], [A-Z], and subsequent character is one of
     *  [a-zA-Z], underscore which is then followed by an optional string "=true" or "=false". Note that for a precise reason
     * (i.e due to {@link #NEG_LONG_BOOLEAN_OPTION_REGEX}), a hyphen is not allowed in the name of such option.
     *
     */
    static final String LONG_BOOLEAN_OPTION_REGEX        = "^--[\\w && \\D][_\\w]+(=([t][r][u][e]|[f][a][l][s][e]))?$";

    /** A <b> regex </b> based on {@link #NEG_LONG_PREFIX} for a boolean option with long name negated.
     *  Put literally, it is a string that starts with NEG_LONG_PREFIX followed by a valid long name, derived from
     *  #LONG_BOOLEAN_OPTION_REGEX. 
     *
     */
    static final String NEG_LONG_BOOLEAN_OPTION_REGEX    = "^" + NEG_LONG_PREFIX +"[\\w &&\\D][_\\w]+$";

    /** A <b> regex </b> for a non-boolean option (which could be boolean as well, if the values specified are "true"
     *  or "false"). Put literally, it is a hyphen followed by a single character in the range [a-zA-Z] and then
     *  an optional specification of equal to sign and value (which can be anything one or more character long).
     *
     */
    static final String SHORT_NON_BOOLEAN_OPTION_REGEX   = "^-[a-zA-Z](=(.)+)?$";

    /** A <b> regex </b> for a non-boolean option (which could be boolean as well, if the values specified are "true"
     *  or "false"). Put literally, it is two hyphens followed by a valid option name based on #LONG_BOOLEAN_OPTION_REGEX then
     *  an optional specification of equal to sign and value (which can be anything one or more character long).
     */
    static final String LONG_NON_BOOLEAN_OPTION_REGEX    = "^--[\\w && \\D][_\\w]+(=(.)+)?$";

    /** A fixed string "--" and nothing else.
     */
    static final String REMAINDER_ARE_OPERANDS_INDICATOR_REGEX = "^--$";

    /** A separator for name and value of an option in the same argument. The first occurrance of this character
     *  separates name and value of an option within the same string.
     */
    static final String OPTION_NAME_VALUE_SEPARATOR    = "=";

    private ParseUtilities() {}

    /** Tests if the argument represents a command name.
     * @param s A string representing an argument on command line
     * @return true if argument matches #CMD_NAME_REGEX, false otherwise
     * @throws NullPointerException if argument is null
     */
    static boolean indicatesCommandName(String s) {
        return matches(s, CMD_NAME_REGEX);
    }

    /** Tests if the argument represents any kind of <b> single</b> option. This excludes a boolean option list which
     *  represents two or more boolean options.
     * @param s A string representing an argument on command line
     * @return true if argument matches #SHORT_BOOLEAN_OPTION_REGEX, #LONG_BOOLEAN_OPTION_REGEX, #NEG_LONG_BOOLEAN_OPTION_REGEX,
     * #SHORT_NON_BOOLEAN_OPTION_REGEX, or #LONG_NON_BOOLEAN_OPTION_REGEX, false otherwise
     * @throws NullPointerException if argument is null
     */
    static boolean indicatesOption(String s) {
        return indicatesShortOption(s) || indicatesLongOption(s);
    }

    /** Tests if the argument represents a boolean option list.
     *
     * @param s String representing argument
     * @return true if argument matches #SHORT_BOOLEAN_OPTION_LIST_REGEX, false otherwise
     * @throws NullPointerException if argument is null
     */
    static boolean indicatesBooleanOptionList(String s) {
        return matches(s, SHORT_BOOLEAN_OPTION_LIST_REGEX);
    }
    
    /** Tests if the argument represents any kind of <b> single</b> option. This excludes a boolean option list which
     *  represents two or more boolean options.
     * @param s A string representing an argument on command line
     * @return true if argument matches #SHORT_BOOLEAN_OPTION_REGEX,
     * #SHORT_BOOLEAN_OPTION_REGEX, or #SHORT_NON_BOOLEAN_OPTION_REGEX, false otherwise
     * @throws NullPointerException if argument is null
     */
    static boolean indicatesShortOption(String s) {
        return indicatesShortBooleanOption(s) || indicatesShortNonBooleanOption(s);
    }

    /** Tests if the argument represents any kind of <b> single</b> option. This excludes a boolean option list which
     *  represents two or more boolean options.
     * @param s A string representing an argument on command line
     * @return true if argument matches #SHORT_BOOLEAN_OPTION_REGEX, #LONG_BOOLEAN_OPTION_REGEX, #NEG_LONG_BOOLEAN_OPTION_REGEX,
     * #SHORT_NON_BOOLEAN_OPTION_REGEX, or #LONG_NON_BOOLEAN_OPTION_REGEX, false otherwise
     * @throws NullPointerException if argument is null
     */
    static boolean indicatesLongOption(String s) {
        return indicatesLongBooleanOption(s) || indicatesNegativeLongBooleanOption(s) || indicatesLongNonBooleanOption(s);
    }

    static boolean indicatesShortBooleanOption(String s) {
        return matches(s, SHORT_BOOLEAN_OPTION_REGEX);
    }

    static boolean indicatesLongBooleanOption(String s) {
        return matches(s, LONG_BOOLEAN_OPTION_REGEX);
    }
    static boolean indicatesNegativeLongBooleanOption(String s) {
        return matches(s, NEG_LONG_BOOLEAN_OPTION_REGEX);
    }
    static boolean indicatesShortNonBooleanOption(String s) {
        return matches(s, SHORT_NON_BOOLEAN_OPTION_REGEX);
    }

    static boolean indicatesLongNonBooleanOption(String s) {
        return matches(s, LONG_NON_BOOLEAN_OPTION_REGEX);
    }

    static boolean indicatesEndOfOptions(String s) {
        return matches(s, REMAINDER_ARE_OPERANDS_INDICATOR_REGEX);
    }

    /** Returns the <code> long name </code> of the option specified on the command line.
     *
     * @param s the option as it appears on command line, e.g. <code> --host or --host=value or --no-host</code>
     * @return name of the option e.g. <code>host</code> from given param
     */
    static String getOptionNameFromLongOption(String s) {
        if (!indicatesLongBooleanOption(s) &&
            !indicatesLongNonBooleanOption(s) &&
            !indicatesNegativeLongBooleanOption(s))
            throw new IllegalArgumentException(s + " does not specify a long option");
        if (indicatesLongBooleanOption(s) || indicatesLongNonBooleanOption(s)) {
            int start = 2; //character after "--"
            int equalsIndex = s.indexOf(OPTION_NAME_VALUE_SEPARATOR);
            int end = equalsIndex == -1 ? s.length() : equalsIndex;
            return s.substring(start, end);
        } else { //has to be --no-name
            return s.substring(NEG_LONG_PREFIX.length(), s.length());
        }
    }
    /** Returns the <code> symbol </code> of the option specified on the command line as a short option.
     *
     * @param s the option as it appears on command line, e.g. <code> -f or -f=value</code>
     * @return symbol of the option e.g. <code>f</code> from given param
     */
    static char getOptionSymbolFromShortOption(String s) {
        if (!indicatesShortBooleanOption(s) &&
            !indicatesShortNonBooleanOption(s))
            throw new IllegalArgumentException(s + " does not specify a long option");
        return s.charAt(1); //a symbol 's' is always specified as -s, -s=value
    }

    static boolean hasOptionNameAndValue(String s) {
        if (!indicatesOption(s))
            return false;
        return s.indexOf(OPTION_NAME_VALUE_SEPARATOR) != -1; //should be an option && should have at least one '='
    }

    static String getOptionValue(String s) {
        //makes sense only if the given param has both name/symbol and value specified, delimited by an '='
        if (hasOptionNameAndValue(s)) {
            return s.substring(s.indexOf(OPTION_NAME_VALUE_SEPARATOR) + 1);
        }
        return null;
    }

    static void booleanOptionListToOptionMap(String s, Map<Character, String> trueOptions) {
        if (indicatesBooleanOptionList(s)) {
            String optionMnemonicsCombined = s.substring(1);
            for (char ch : optionMnemonicsCombined.toCharArray())
                trueOptions.put(ch, "true");
        }
        //this method could have thrown an exception for a string that does NOT represent list of boolean options
    }

    static OptionDesc getOptionDescForName(String s, Set<OptionDesc> som) {
        for (OptionDesc od : som) {
            if (od.getName().equals(s))
                return od;
        }
        return null;
    }

    static OptionDesc getOptionDescForSymbol(char c, Set<OptionDesc> som) {
        String sc = Character.toString(c);
        for (OptionDesc od : som) {
            if (od.getSymbol().equals(sc))
                return od;
        }
        return null;
    }

    static OptionDesc getOptionDescForBooleanOptionForName(String s, Set<OptionDesc> som) {
     for (OptionDesc od : som) {
         if (od.getName().equals(s)) {
            if (od.getType().equals(OptionType.BOOLEAN.name()))
                return od;
         }
     }
        return null;
    }

    static OptionDesc getOptionDescForBooleanOptionForSymbol(char c, Set<OptionDesc> som) {
        String sc = Character.toString(c);
        for (OptionDesc od : som) {
            if (od.getSymbol().equals(sc)) {
                if (od.getType().equals(OptionType.BOOLEAN.name()))
                    return od;
            }
        }
        return null;
    }
    static OptionDesc getMetadataFor(String s, Set<OptionDesc> som) {
        for(OptionDesc od : som) {
            if (od.getName().equals(s))
                return od;
        }
        return null;
    }

    static Set<OptionDesc> getAllOptionMetadataExcluding(Set<OptionDesc> som, Set<String> names) {
        if (names == null || names.isEmpty())
            return som;
        Set<OptionDesc> remaining = new HashSet<OptionDesc>(som); //make a copy first
        Set<OptionDesc> exclude = new HashSet<OptionDesc>();
        for (String name : names) {
            for (OptionDesc od : som) {
                if (od.getName().equals(name)) {
                    exclude.add(od);
                    break;
                }
            }
        }
        remaining.removeAll(exclude); //no need to have this set unmodifiable as it represents no state here in this class
        return remaining;
    }

    static boolean nonNullValueValidFor(OptionDesc po, String value) {
        if (value == null)
                return true;
        //VERY basic validation only if given value is non-null
        if (po == null)
            throw new IllegalArgumentException ("null arg");
        value = value.trim();

        if (OptionType.FILE.name().equals(po.getType())) {
            File f = new File(value);
            return f.isFile() || f.canRead();
        }
        if (OptionType.BOOLEAN.name().equals(po.getType())) {
            return ("true".equals(value.toLowerCase()) || "false".equals(value.toLowerCase()));
        }
        //non-null value for any remaining option is valid
        return true;
    }

    static char getSymbol(String name, Map<Character, String>symbolToName) {
        for (char symbol : symbolToName.keySet()) {
            String aName = symbolToName.get(symbol);
            if (aName.equals(name))
                return symbol;
        }
        return '\u0000'; // this character implies no such symbol
    }

    static String getName(char symbol, Map<Character, String>symbolToName) {
        return symbolToName.get(symbol); //null implies no mapping
    }

    static Map<Character, String> getSymbolToNameMap(Set<OptionDesc> som) {
        Map<Character, String> s2n = new HashMap<Character, String>();
        for (OptionDesc od : som)
            s2n.put(od.getSymbol().charAt(0), od.getName());
        return s2n;
    }
    
    static void file2Set(String file, Set<String> set) {
        BufferedReader reader = null;
        try {
            InputStream is = ParseUtilities.class.getClassLoader().getResourceAsStream(file);
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = reader.readLine()) != null) {
                if (line.startsWith("#"))
                    continue; //# indicates comment
                StringTokenizer tok = new StringTokenizer(line, " "); //space delimited
                String cmd = tok.nextToken();   //handles with or without space, rudimendary as of now
                set.add(cmd);
            }

        } catch(IOException e) {
          e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch(IOException ee) {
                    //ignore
                }

            }
        }
    }
    static Option getOptionNamed(Set<Option> ops, String name) {
        for (Option op : ops)
            if (op.getName().equals(name))
                return op;
        return null;
    }

    static boolean isPassword(OptionDesc od) {
        return OptionType.PASSWORD.name().equals(od.getType());
    }
    // ALL Private ...

    private static boolean matches(String s, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        return m.matches();
    }
}
