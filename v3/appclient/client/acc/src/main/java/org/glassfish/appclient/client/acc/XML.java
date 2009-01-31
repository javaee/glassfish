/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.glassfish.appclient.client.acc;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Tim
 */
public class XML {

    private static final List<String> booleanTrueValues = Arrays.asList("yes", "on", "1", "true");

    private static final List<String> providerTypeValues = Arrays.asList("client", "server", "client-server");

    public static boolean parseBoolean(final String booleanText) {
        return _parseBoolean(booleanText.trim());
    }

    private static boolean isWhiteSpace(char ch) {
        return ch==' ' || ch == '\t';
    }
    /**
     *
     * <code><!ENTITY % boolean "(yes | no | on | off | 1 | 0 | true | false)"></code>
     *
     * @param literal
     * @return
     */
    private static boolean _parseBoolean(final CharSequence literal) {
        int i=0;
        int len = literal.length();
        char ch;
        do {
            ch = literal.charAt(i++);
        } while(isWhiteSpace(ch) && i<len);

        // if we are strict about errors, check i==len. and report an error

        return booleanTrueValues.contains(literal.subSequence(i, len));
    }

    public static String parseProviderType(String providerType) {
        if (providerTypeValues.contains(providerType)) {
            return providerType;
        }
        throw new IllegalArgumentException(providerType);
    }
}
