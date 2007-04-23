package com.sun.enterprise.module.maven;

/**
 * @author Kohsuke Kawaguchi
 */
public class TokenListBuilder {
    private final StringBuilder builder = new StringBuilder();
    private final String delimiter;

    public TokenListBuilder(String delimiter) {
        this.delimiter = delimiter;
    }

    public TokenListBuilder() {
        this(", ");
    }

    public void add(Object o) {
        if(builder.length()>0)
            builder.append(delimiter);
        builder.append(o.toString());
    }

    public boolean isEmpty() {
        return builder.length()==0;
    }

    public String toString() {
        return builder.toString();
    }
}
