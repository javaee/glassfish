package com.sun.enterprise.module.maven;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class IOException2 extends IOException {
    public IOException2(String s) {
        super(s);
    }

    public IOException2(String s,Throwable t) {
        super(s);
        initCause(t);
    }
}
