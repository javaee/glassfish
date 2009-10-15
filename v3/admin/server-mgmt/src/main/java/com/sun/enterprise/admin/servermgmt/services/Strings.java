/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.admin.servermgmt.services;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * Package-wide String getter
 * @author bnevins
 */
public class Strings {
    private Strings() {
        // no instances allowed
    }

    public static String get(String indexString, Object... objects) {
        return strings.get(indexString, objects);
    }

    public static String get(String indexString) {
        return strings.get(indexString);
    }

    private final static LocalStringsImpl strings = new LocalStringsImpl(Strings.class);
}
