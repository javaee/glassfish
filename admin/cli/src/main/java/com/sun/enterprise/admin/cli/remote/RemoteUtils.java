
package com.sun.enterprise.admin.cli.remote;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 *
 * @author bnevins
 */
public class RemoteUtils {
    static String getString(String s) {
        return strings.get(s);
    }

    static String getString(String s, Object... objs) {
        return strings.get(s, objs);
    }
    private final static LocalStringsImpl strings = new LocalStringsImpl(RemoteUtils.class);
}
