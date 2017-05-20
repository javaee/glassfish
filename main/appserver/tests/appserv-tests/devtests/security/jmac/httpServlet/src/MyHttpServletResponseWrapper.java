/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
