package com.sun.hk2.component;

import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Parses a string like <tt>key=value,key=value,key=value</tt>.
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

    public void parseNext() {
        int del = str.indexOf('=',idx+1);
        if(del==-1) del=str.length();

        int end = str.indexOf(',',idx+1);
        if(end==-1) end=str.length();

        if(del>=end) {
            // key only, no value
            key = str.substring(idx,end);
            value = null;
        } else {
            // key=value
            key = str.substring(idx,del);
            value = str.substring(del+1,end);
        }

        idx = end;
        if(idx<str.length()) idx++; // skip ','
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
