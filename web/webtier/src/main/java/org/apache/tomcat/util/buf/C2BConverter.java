

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


package org.apache.tomcat.util.buf;

import org.apache.tomcat.util.buf.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CoderResult;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/** Efficient conversion of character to bytes.
 *  
 *  Now uses NIO directly
 */

public class C2BConverter {

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog(C2BConverter.class );

    protected ByteChunk bb;
    protected String enc;
    protected CharsetEncoder encoder;
    
    /** Create a converter, with bytes going to a byte buffer
     */
    public C2BConverter(ByteChunk output, String encoding) throws IOException {
        this.bb=output;
        this.enc=encoding;
        encoder = Charset.forName(enc).newEncoder().
		onMalformedInput(CodingErrorAction.REPLACE).
		onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    /** Create a converter
     */
    public C2BConverter(String encoding) throws IOException {
        this( new ByteChunk(1024), encoding );
    }

    public static C2BConverter getInstance(ByteChunk output, String encoding) throws IOException {
        return new C2BConverter(output, encoding);
    }
    
    public ByteChunk getByteChunk() {
        return bb;
    }

    public String getEncoding() {
        return enc;
    }

    public void setByteChunk(ByteChunk bb) {
        this.bb=bb;
    }

    /** Reset the internal state, empty the buffers.
     *  The encoding remain in effect, the internal buffers remain allocated.
     */
    public  void recycle() {
        bb.recycle();
    }

    /** Generate the bytes using the specified encoding
     */
    public void convert(char c[], int off, int len) throws IOException {
        CharBuffer cb = CharBuffer.wrap(c, off, len);
        byte[] barr = bb.getBuffer();
        int boff = bb.getEnd();
        ByteBuffer tmp = ByteBuffer.wrap(barr, boff, barr.length - boff);
        CoderResult cr = encoder.encode(cb, tmp, true);
        bb.setEnd(tmp.position());
        while (cr == CoderResult.OVERFLOW) {
	    if (!bb.canGrow())
                bb.flushBuffer();
	    boff = bb.getEnd();
	    barr = bb.getBuffer();
            tmp = ByteBuffer.wrap(barr, boff, barr.length - boff);
            cr = encoder.encode(cb, tmp, true);
            bb.setEnd(tmp.position());
        }
        if (cr != CoderResult.UNDERFLOW) {
            throw new IOException("Encoding error");
	}
    }

    /** Generate the bytes using the specified encoding
     */
    public  void convert(String s ) throws IOException {
        convert(s, 0, s.length());
    }
    
    /** Generate the bytes using the specified encoding
     */    
    public  void convert(String s, int off, int len ) throws IOException {
        convert(s.toCharArray(), off, len);
    }

    /** Generate the bytes using the specified encoding
     */
    public  void convert(char c ) throws IOException {
        char[] tmp = new char[1];
        tmp[0] = c;
        convert(tmp, 0, 1);
    }

    /** Convert a message bytes chars to bytes
     */
    public void convert(MessageBytes mb ) throws IOException {
        int type=mb.getType();
        if( type==MessageBytes.T_BYTES )
            return;
        ByteChunk orig=bb;
        setByteChunk( mb.getByteChunk());
        bb.recycle();
        bb.allocate( 32, -1 );
        
        if( type==MessageBytes.T_STR ) {
            convert( mb.getString() );
            // System.out.println("XXX Converting " + mb.getString() );
        } else if( type==MessageBytes.T_CHARS ) {
            CharChunk charC=mb.getCharChunk();
            convert( charC.getBuffer(),
                                charC.getOffset(), charC.getLength());
            //System.out.println("XXX Converting " + mb.getCharChunk() );
        } else {
            if (log.isDebugEnabled()) 
                log.debug("XXX unknowon type " + type );
        }
        //System.out.println("C2B: XXX " + bb.getBuffer() + bb.getLength()); 
        setByteChunk(orig);
    }

    /** Flush any internal buffers into the ByteOutput or the internal
     *  byte[]
     */
    public  void flushBuffer() throws IOException {
        bb.flushBuffer();
    }

}
