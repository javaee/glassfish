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
package com.sun.enterprise.module.common_impl;

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
