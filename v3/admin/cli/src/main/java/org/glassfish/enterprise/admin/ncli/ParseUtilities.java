package org.glassfish.enterprise.admin.ncli;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.Set;
import java.util.StringTokenizer;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

/**
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
final class ParseUtilities {

    private static final String NEG_LONG_PREFIX               = "--no-";
    private static final String CMD_NAME_REGEX                = "^[a-zA-Z]([-_\\w]*)?$";
    private static final String SHORT_BOOLEAN_OPTION_REGEX    = "^-[a-zA-Z](=([t][r][u][e]|[f][a][l][s][e]))?$";
    private static final String LONG_BOOLEAN_OPTION_REGEX     = "^--[\\w && \\D][-_\\w]+(=([t][r][u][e]|[f][a][l][s][e]))?$";
    private static final String NEG_LONG_BOOLEAN_OPTION_REGEX = "^" + NEG_LONG_PREFIX +"[\\w &&\\D][-_\\w]+$";

    private static final String SHORT_NON_BOOLEAN_OPTION_REGEX = "^-[a-zA-Z](=(.)+)?$";
    private static final String LONG_NON_BOOLEAN_OPTION_REGEX  = "^--[\\w && \\D][-_\\w]+(=(.)+)?$";

    private static final String REMAINDER_ARE_OPERANDS_INDICATOR_REGEX = "^--$";
    
    private static final String OPTION_NAME_VALUE_SEPARATOR    = "=";



    private ParseUtilities() {}

    static boolean indicatesCommandName(String s) {
        return matches(s, CMD_NAME_REGEX);
    }
    
    static boolean indicatesOption(String s) {
        return indicatesShortBooleanOption(s)        ||
               indicatesLongBooleanOption(s)         ||
               indicatesNegativeLongBooleanOption(s) ||
               indicatesShortNonBooleanOption(s)     ||
               indicatesLongNonBooleanOption(s);
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
    static String getOptionSymbolFromShortOption(String s) {
        if (!indicatesShortBooleanOption(s) &&
            !indicatesShortNonBooleanOption(s))
            throw new IllegalArgumentException(s + " does not specify a long option");
        return s.substring(1, 2); //a symbol 's' is always specified as -s, -s=value
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
    // ALL Private ...
    private static boolean matches(String s, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        return m.matches();
    }
}
