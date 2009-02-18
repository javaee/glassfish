
package org.glassfish.embed.util;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 *
 * @author bnevins
 */
public class StringHelper {
    public static String get(String index) {
        return strings.get(index);
    }

    public static String get(String index, Object... objs) {
        return strings.get(index, objs);
    }
    
    private static final LocalStringsImpl strings = new LocalStringsImpl(StringHelper.class);
    
    private StringHelper() {
    }
}
