/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.hk2.component;

import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Parses a string like <tt>key=value,key=value,key=value</tt>.
 *
 * <p>
 * More specifically the format of the line satisfies the following BNF constructions:
 *
 * <pre>
 * LINE = TOKEN ( ',' LINE )?               // LINE is ','-separated TOKENs
 * TOKEN = KEY ( '=' VALUE )?               // TOKEN is a key/value pair. value is optional.
 * KEY = [^,=]+                             // KEY is a non-empty string that doesn't contain ',' nor '='
 * VALUE = '"' ([^"]| '\' ANYCHAR )* '"'    // VALUE is any string surrounded by quotes/doublequotes (where \ is used as escape), or ...
 * VALUE = ''' ([^']| '\' ANYCHAR )* '''
 * VALUE = [^,]*                //       possibly empty string that doesn't contain ','
 * </pre>
 *
 * <p>
 * This class works like {@link StringTokenizer}; each time {@link #parseNext()}
 * is invoked, the parser "moves" to the next key/value pair, which
 * you can then obtain with {@link #getKey()} and {@link #getValue()}.
 * The {@link #hasNext()} method shall be used to check if there's a next key/value
 * pair after the current position.
 *
 * TODO: improved error check
 * @author Kohsuke Kawaguchi
 */
public final class KeyValuePairParser {
    private String str;
    private int idx;

    private String key;
    private String value;
    private final StringBuilder buf = new StringBuilder();

    public KeyValuePairParser() {
    }

    public KeyValuePairParser(String s) {
        set(s);
    }

    /**
     * Resets the parser to parse the given string that looks like "key=value,key=value,..."
     */
    public void set(String str) {
        this.str = str;
        idx = 0;
    }

    public boolean hasNext() {
        return idx<str.length();
    }

    private int indexOf(char separator) {
        int i = str.indexOf(separator, idx + 1);
        if(i<0) return str.length();
        return i;
    }

    /**
     * Returns true if the current character is ch.
     */
    private boolean isAt(char ch) {
        return idx<str.length() && str.charAt(idx)==ch;
    }

    private char current() {
        return str.charAt(idx);
    }

    public void parseNext() {
        // parse key
        int del = Math.min(indexOf('='),indexOf(','));
        key = str.substring(idx,del);
        value = null;
        idx=del;

        if(idx==str.length())
            return; // key only, and we are at the end of the line

        if(current()==',') {
            // key only, so consume ',' and return.
            idx++;
            return;
        }

        // we have a value. now consume '='
        assert current()=='=';
        idx++;

        if(isAt('\'') || isAt('"')) {
            char quote = current();
            // quoted string. now let's find the end:
            idx++;
            buf.setLength(0);
            while(true) {
                if(isAt(quote)) {
                    value = buf.toString();
                    // consume endquote and ','. this may put as beyond EOL but that's fine.
                    idx+=2;
                    return;
                }
                if(isAt('\\'))
                    idx++;
                buf.append(current());
                idx++;
            }
        }

        // unquoted string
        int end = indexOf(',');
        value = str.substring(idx,end);
        idx=end+1;
    }


    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    /**
     * Moves the parser to the head of the key/value pair, to reparse the same string.
     */
    public void rewind() {
        idx=0;
    }

    /**
     * Skips until we find the value of the given key,
     * and leaves the parser at that point.
     * <p>
     * This method is not terribly efficient, but it is convenient.
     */
    public String find(String key) {
        while(hasNext()) {
            parseNext();
            if(getKey().equals(key))
                return getValue();
        }
        return null;
    }

    /**
     * Finds all the values of the given key.
     * Calling the iterator will rewind the parser.
     */
    public Iterable<String> findAll(final String key) {
        return new Iterable<String>() {
            public Iterator<String> iterator() {
                rewind();
                return new Iterator<String>() {
                    private String next;
                    private void fetch() {
                        if(next==null)
                            next = find(key);
                    }
                    public boolean hasNext() {
                        fetch();
                        return next!=null;
                    }

                    public String next() {
                        fetch();
                        String r = next;
                        next = null;
                        return r;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Gets the whole line.
     */
    public String getLine() {
        return str;
    }
}
