package org.glassfish.flashlight.impl.client;

/**
 * @author Sreenivas Munnangi
 *         Date: 04aug2009
 */

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlashLightBTracePrintWriter extends PrintWriter {

    private final Logger l;
    private final Level logLevel = Level.WARNING ;
    private final String BTRACE_PREFIX = "btrace:";

    FlashLightBTracePrintWriter(OutputStream out, Logger l) {
        super(out);
        this.l = l;
    }

    public void print(boolean b) {
        this.println(b);
    }

    public void print(char c) {
        this.println(c);
    }

    public void print(char[] cArr) {
        this.println(cArr);
    }

    public void print(double d) {
        this.println(d);
    }

    public void print(float f) {
        this.println(f);
    }

    public void print(int i) {
        this.println(i);
    }

    public void print(long lng) {
        this.println(lng);
    }

    public void print(Object o) {
        this.println(o);
    }

    public void print(String s) {
        this.println(s);
    }


    public void println() {
    }

    public void println(boolean b) {
        l.log(logLevel, BTRACE_PREFIX + String.valueOf(b));
    }

    public void println(char c) {
        l.log(logLevel, BTRACE_PREFIX + String.valueOf(c));
    }

    public void println(char[] cArr) {
        l.log(logLevel, BTRACE_PREFIX + String.valueOf(cArr));
    }

    public void println(double d) {
        l.log(logLevel, BTRACE_PREFIX + String.valueOf(d));
    }

    public void println(float f) {
        l.log(logLevel, BTRACE_PREFIX + String.valueOf(f));
    }

    public void println(int i) {
        l.log(logLevel, BTRACE_PREFIX + String.valueOf(i));
    }

    public void println(long lng) {
        l.log(logLevel, BTRACE_PREFIX + String.valueOf(lng));
    }

    public void println(Object o) {
        l.log(logLevel, BTRACE_PREFIX + o.toString());
    }

    public void println(String s) {
        l.log(logLevel, BTRACE_PREFIX + s);
    }

    
}
