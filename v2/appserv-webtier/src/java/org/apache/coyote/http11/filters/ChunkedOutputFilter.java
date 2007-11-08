

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
import org.apache.tomcat.util.buf.HexUtils;

import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;
import org.apache.coyote.http11.OutputFilter;

/**
 * Chunked output filter.
 * 
 * @author Remy Maucherat
 */
public class ChunkedOutputFilter implements OutputFilter {


    // -------------------------------------------------------------- Constants


    protected static final String ENCODING_NAME = "chunked";
    protected static final ByteChunk ENCODING = new ByteChunk();


    /**
     * End chunk.
     */
    protected static final ByteChunk END_CHUNK = new ByteChunk();


    // ----------------------------------------------------- Static Initializer


    static {
        ENCODING.setBytes(ENCODING_NAME.getBytes(), 0, ENCODING_NAME.length());
        String endChunkValue = "0\r\n\r\n";
        END_CHUNK.setBytes(endChunkValue.getBytes(), 
                           0, endChunkValue.length());
    }


    // ------------------------------------------------------------ Constructor


    /**
     * Default constructor.
     */
    public ChunkedOutputFilter() {
        chunkLength = new byte[10];
        chunkLength[8] = (byte) '\r';
        chunkLength[9] = (byte) '\n';
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Next buffer in the pipeline.
     */
    protected OutputBuffer buffer;


    /**
     * Buffer used for chunk length conversion.
     */
    protected byte[] chunkLength = new byte[10];


    /**
     * Chunk header.
     */
    protected ByteChunk chunkHeader = new ByteChunk();


    // ------------------------------------------------------------- Properties


    // --------------------------------------------------- OutputBuffer Methods


    /**
     * Write some bytes.
     * 
     * @return number of bytes written by the filter
     */
    public int doWrite(ByteChunk chunk, Response res)
        throws IOException {

        int result = chunk.getLength();

        if (result <= 0) {
            return 0;
        }

        // Calculate chunk header
        int pos = 7;
        int current = result;
        while (current > 0) {
            int digit = current % 16;
            current = current / 16;
            chunkLength[pos--] = HexUtils.HEX[digit];
        }
        chunkHeader.setBytes(chunkLength, pos + 1, 9 - pos);
        buffer.doWrite(chunkHeader, res);

        buffer.doWrite(chunk, res);

        chunkHeader.setBytes(chunkLength, 8, 2);
        buffer.doWrite(chunkHeader, res);

        return result;

    }


    // --------------------------------------------------- OutputFilter Methods


    /**
     * Some filters need additional parameters from the response. All the 
     * necessary reading can occur in that method, as this method is called
     * after the response header processing is complete.
     */
    public void setResponse(Response response) {
    }


    /**
     * Set the next buffer in the filter pipeline.
     */
    public void setBuffer(OutputBuffer buffer) {
        this.buffer = buffer;
    }


    /**
     * End the current request. It is acceptable to write extra bytes using
     * buffer.doWrite during the execution of this method.
     */
    public long end()
        throws IOException {

        // Write end chunk
        buffer.doWrite(END_CHUNK, null);
        
        return 0;

    }


    /**
     * Make the filter ready to process the next request.
     */
    public void recycle() {
    }


    /**
     * Return the name of the associated encoding; Here, the value is 
     * "identity".
     */
    public ByteChunk getEncodingName() {
        return ENCODING;
    }


}
