

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.coyote.http11.filters;

import java.io.IOException;

import org.apache.tomcat.util.buf.ByteChunk;

import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.http11.InputFilter;

/**
 * Void input filter, which returns -1 when attempting a read. Used with a GET,
 * HEAD, or a similar request.
 * 
 * @author Remy Maucherat
 */
public class VoidInputFilter implements InputFilter {


    // -------------------------------------------------------------- Constants


    protected static final String ENCODING_NAME = "void";
    protected static final ByteChunk ENCODING = new ByteChunk();


    // ----------------------------------------------------- Static Initializer


    static {
        ENCODING.setBytes(ENCODING_NAME.getBytes(), 0, ENCODING_NAME.length());
    }


    // ----------------------------------------------------- Instance Variables


    // --------------------------------------------------- OutputBuffer Methods


    /**
     * Write some bytes.
     * 
     * @return number of bytes written by the filter
     */
    public int doRead(ByteChunk chunk, Request req)
        throws IOException {

        return -1;

    }


    // --------------------------------------------------- OutputFilter Methods


    /**
     * Set the associated reauest.
     */
    public void setRequest(Request request) {
    }


    /**
     * Set the next buffer in the filter pipeline.
     */
    public void setBuffer(InputBuffer buffer) {
    }


    /**
     * Make the filter ready to process the next request.
     */
    public void recycle() {
    }


    /**
     * Return the name of the associated encoding; Here, the value is 
     * "void".
     */
    public ByteChunk getEncodingName() {
        return ENCODING;
    }


    /**
     * End the current request. It is acceptable to write extra bytes using
     * buffer.doWrite during the execution of this method.
     * 
     * @return Should return 0 unless the filter does some content length 
     * delimitation, in which case the number is the amount of extra bytes or
     * missing bytes, which would indicate an error. 
     * Note: It is recommended that extra bytes be swallowed by the filter.
     */
    public long end()
        throws IOException {
        return 0;
    }


}
