/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.s1asdev.security.jmac.httpservlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class MyHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private MyPrintWriter myPrintWriter = null;

    MyHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        try {
            myPrintWriter = new MyPrintWriter(response.getWriter());
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex.toString());
        }
    }

    public PrintWriter getWriter() throws IOException {
        return myPrintWriter;
    }

    int getAdjustedCount() {
        return myPrintWriter.getAdjustedCount();
    }
}

class MyPrintWriter extends PrintWriter {
    private int count = 0; // count up to '\r'

    public MyPrintWriter(Writer writer) {
        super(writer);
    }

    // our jsp writer only use write char[] off len
    public void write(char[] cbuf, int off, int len) { 
        count += len - numOfCR(cbuf, off, len);
        super.write(cbuf, off, len);
    }

    public int getAdjustedCount() {
        return count;
    }

    private int numOfCR(char[] cbuf, int off, int len) {
        int numCR = 0;
        if (cbuf != null && off < cbuf.length) {
            for (int i = off; i <= len -1 && i < cbuf.length; i++) {
                if (cbuf[i] == '\r') {
                    numCR++;
                }
            }
        }
        return numCR;
    }
}
