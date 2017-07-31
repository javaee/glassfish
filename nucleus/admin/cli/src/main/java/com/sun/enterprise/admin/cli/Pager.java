/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Pager.java
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 */

class Pager {
    private BufferedReader in;
    private BufferedWriter out;
    private int pageLength;
    private String line;

    /**
     * Construct an object which will copy one pages worth of lines
     * at a time from the input to the
     * the output.
     *
     * No attempt is made under any circumstances to close the input
     * or output.
     *
     * @param lines the number of lines in a page. A number less
     * than 0 means copy all the input to the output.
     * @param in the source of the copy operation
     * @param out the destination of the copy operation
     * @throws IOException if there's a problem reading from, or
     * writing to, the source or destination
     */
    Pager(int lines, Reader in, Writer out) throws IOException {
        this.in = new BufferedReader(in);
        this.out = new BufferedWriter(out);
        pageLength = lines;
        nextLine();
    }

    /**
     * Copy the next page worth of lines from input to output
     */
    void nextPage() throws IOException {
        for (int i = 0; (pageLength < 0 || i < pageLength) && hasNext(); i++) {
            out.write(line);
            out.newLine();
            nextLine();
        }
        out.flush();
    }

    /**
     * Indicate if there are lines left to be copied
     * @return true iff there is at least one line left to be copied
     */
    boolean hasNext() {
        return line != null;
    }

    /**
     * Get the next line and copy it inot the internal buffer so's
     * we can answer the hasNext() question
     */
    private void nextLine() throws IOException {
        line = in.readLine();
    }
}
