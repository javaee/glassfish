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
