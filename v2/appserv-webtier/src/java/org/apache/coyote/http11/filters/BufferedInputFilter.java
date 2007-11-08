

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
import org.apache.coyote.Request;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.http11.InputFilter;
import org.apache.tomcat.util.buf.ByteChunk;

/**
 * Input filter responsible for reading and buffering the request body, so that
 * it does not interfere with client SSL handshake messages.
 */
public class BufferedInputFilter implements InputFilter {

    // -------------------------------------------------------------- Constants

    private static final String ENCODING_NAME = "buffered";
    private static final ByteChunk ENCODING = new ByteChunk();


    // ----------------------------------------------------- Instance Variables

    private ByteChunk buffered = null;
    private ByteChunk tempRead = new ByteChunk(1024);
    private InputBuffer buffer;
    private boolean hasRead = false;


    // ----------------------------------------------------- Static Initializer

    static {
        ENCODING.setBytes(ENCODING_NAME.getBytes(), 0, ENCODING_NAME.length());
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Set the buffering limit. This should be reset every time the buffer is
     * used.
     */
    public void setLimit(int limit) {
        if (buffered == null) {
            buffered = new ByteChunk(4048);
            buffered.setLimit(limit);
        }
    }


    // ---------------------------------------------------- InputBuffer Methods


    /**
     * Reads the request body and buffers it.
     */
    public void setRequest(Request request) {
        // save off the Request body
        try {
            while (buffer.doRead(tempRead, request) >= 0) {
                buffered.append(tempRead);
                tempRead.recycle();
            }
        } catch(IOException iex) {
            // Ignore
        }
    }

    /**
     * Fills the given ByteChunk with the buffered request body.
     */
    public int doRead(ByteChunk chunk, Request request) throws IOException {
        if (hasRead || buffered.getLength() <= 0) {
            return -1;
        } else {
            chunk.setBytes(buffered.getBytes(), buffered.getStart(),
                           buffered.getLength());
            hasRead = true;
        }
        return chunk.getLength();
    }

    public void setBuffer(InputBuffer buffer) {
        this.buffer = buffer;
    }

    public void recycle() {
        if (buffered.getBuffer().length > 65536) {
            buffered = null;
        } else {
            buffered.recycle();
        }
        tempRead.recycle();
        hasRead = false;
        buffer = null;
    }

    public ByteChunk getEncodingName() {
        return ENCODING;
    }

    public long end() throws IOException {
        return 0;
    }

}
