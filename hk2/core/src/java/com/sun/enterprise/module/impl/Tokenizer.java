package com.sun.enterprise.module.impl;

import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.Collections;

/**
 * JDK5-friendly string tokenizer.
 * 
 * @author Kohsuke Kawaguchi
 */
public final class Tokenizer implements Iterable<String> {
    private final String text;
    private final String delimiter;

    /**
     * @param data
     *      Text to be tokenized. Can be null, in which case
     *      the iterator will return nothing.
     * @param delimiter
     *      Passed as a delimiter to {@link StringTokenizer#StringTokenizer(String, String)},.
     */
    public Tokenizer(String data, String delimiter) {
        this.text = data;
        this.delimiter = delimiter;
    }


    public Iterator<String> iterator() {
        if(text==null)
            return Collections.<String>emptyList().iterator();

        return new Iterator<String>() {
            private final StringTokenizer tokens = new StringTokenizer(text,delimiter);

            public boolean hasNext() {
                return tokens.hasMoreTokens();
            }

            public String next() {
                return tokens.nextToken().trim();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
