package org.glassfish.enterprise.admin.ncli;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
final class ParseUtilities {
    private static final String SHORT_BOOLEAN_OPTION_REGEX    = "^-[a-zA-Z](=([t][r][u][e]|[f][a][l][s][e]))?$";
    private static final String LONG_BOOLEAN_OPTION_REGEX     = "^--[\\w && \\D][-_\\w]+(=([t][r][u][e]|[f][a][l][s][e]))?$";
    private static final String NEG_LONG_BOOLEAN_OPTION_REGEX = "^--no-[\\w &&\\D][-_\\w]+$";

    private static final String SHORT_NON_BOOLEAN_OPTION_REGEX = "^-[a-zA-Z](=([^\\s])+)?$";
    private static final String LONG_NON_BOOLEAN_OPTION_REGEX  = "^--[\\w && \\D][-_\\w]+(=([^\\s])+)?$";

    private static final String REMAINDER_ARE_OPERANDS_INDICATOR_REGEX = "^--$";
    
    private static final String OPTION_NAME_VALUE_SEPARATOR    = "=";



    private ParseUtilities() {}

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
    private static boolean matches(String s, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        return m.matches();
    }

}
