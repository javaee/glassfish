
package org.glassfish.embed.util;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 *
 * @author Byron Nevins
 */
public class StringHelper {
    public static String get(String index) {
        return strings.get(index);
    }

    public static String get(String index, Object... objs) {
        return strings.get(index, objs);
    }
    
    private static final LocalStringsImpl strings;

    static {
        strings = new LocalStringsImpl("org.glassfish.embed", "LocalStrings");

        // sanity check
        if("internal".equals(strings.get("internal")))
            throw new RuntimeException("Internal Error: LocalStrings.properties not found.");
    }
    private StringHelper() {
    }
}
