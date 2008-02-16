

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

/** Efficient conversion of bytes  to character .
 *  
 *  This uses the standard JDK mechansim - a reader - but provides mechanisms
 *  to recycle all the objects that are used. It is compatible with JDK1.1
 *  and up,
 *  ( nio is better, but it's not available even in 1.2 or 1.3 )
 *
 *  Not used in the current code, the performance gain is not very big
 *  in the current case ( since String is created anyway ), but it will
 *  be used in a later version or after the remaining optimizations.
 */
public class B2CConverter {

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog( B2CConverter.class );

    private IntermediateInputStream iis;
    private ReadConvertor conv;
    private String encoding;

    protected B2CConverter() {
    }
    
    /** Create a converter, with bytes going to a byte buffer
     */
    public B2CConverter(String encoding)
	throws IOException
    {
	this.encoding=encoding;
	reset();
    }

    
    /** Reset the internal state, empty the buffers.
     *  The encoding remain in effect, the internal buffers remain allocated.
     */
    public  void recycle() {
	conv.recycle();
    }

    static final int BUFFER_SIZE=8192;
    char result[]=new char[BUFFER_SIZE];

    /** Convert a buffer of bytes into a chars
     */
    public  void convert( ByteChunk bb, CharChunk cb )
	throws IOException
    {
	// Set the ByteChunk as input to the Intermediate reader
	iis.setByteChunk( bb );
	convert(cb);
    }

    private void convert(CharChunk cb)
	throws IOException
    {
	try {
	    // read from the reader
	    while( true ) { // conv.ready() ) {
		int cnt=conv.read( result, 0, BUFFER_SIZE );
		if( cnt <= 0 ) {
		    // End of stream ! - we may be in a bad state
		    if( debug>0)
			log( "EOF" );
		    //		    reset();
		    return;
		}
		if( debug > 1 )
		    log("Converted: " + new String( result, 0, cnt ));

		// XXX go directly
		cb.append( result, 0, cnt );
	    }
	} catch( IOException ex) {
	    if( debug>0)
		log( "Reseting the converter " + ex.toString() );
	    reset();
	    throw ex;
	}
    }

    // START CR 6309511
    /**
     * Character conversion of a US-ASCII MessageBytes.
     */
    public static void convertASCII(MessageBytes mb) {
 
        // This is of course only meaningful for bytes
        if (mb.getType() != MessageBytes.T_BYTES)
            return;
        
        ByteChunk bc = mb.getByteChunk();
        CharChunk cc = mb.getCharChunk();
        int length = bc.getLength();
        cc.allocate(length, -1);

        // Default encoding: fast conversion
        byte[] bbuf = bc.getBuffer();
        char[] cbuf = cc.getBuffer();
        int start = bc.getStart();
        for (int i = 0; i < length; i++) {
            cbuf[i] = (char) (bbuf[i + start] & 0xff);
        }
        mb.setChars(cbuf, 0, length);
   
     }
    // END CR 6309511

    public void reset()
	throws IOException
    {
	// destroy the reader/iis
	iis=new IntermediateInputStream();
	conv=new ReadConvertor( iis, encoding );
    }

    private final int debug=0;
    void log( String s ) {
        if (log.isDebugEnabled())
	    log.debug("B2CConverter: " + s );
    }

    // -------------------- Not used - the speed improvemnt is quite small

    /*
    private Hashtable decoders;
    public static final boolean useNewString=false;
    public static final boolean useSpecialDecoders=true;
    private UTF8Decoder utfD;
    // private char[] conversionBuff;
    CharChunk conversionBuf;


    private  static String decodeString(ByteChunk mb, String enc)
	throws IOException
    {
	byte buff=mb.getBuffer();
	int start=mb.getStart();
	int end=mb.getEnd();
	if( useNewString ) {
	    if( enc==null) enc="UTF8";
	    return new String( buff, start, end-start, enc );
	}
	B2CConverter b2c=null;
	if( useSpecialDecoders &&
	    (enc==null || "UTF8".equalsIgnoreCase(enc))) {
	    if( utfD==null ) utfD=new UTF8Decoder();
	    b2c=utfD;
	}
	if(decoders == null ) decoders=new Hashtable();
	if( enc==null ) enc="UTF8";
	b2c=(B2CConverter)decoders.get( enc );
	if( b2c==null ) {
	    if( useSpecialDecoders ) {
		if( "UTF8".equalsIgnoreCase( enc ) ) {
		    b2c=new UTF8Decoder();
		}
	    }
	    if( b2c==null )
		b2c=new B2CConverter( enc );
	    decoders.put( enc, b2c );
	}
	if( conversionBuf==null ) conversionBuf=new CharChunk(1024);

	try {
	    conversionBuf.recycle();
	    b2c.convert( this, conversionBuf );
	    //System.out.println("XXX 1 " + conversionBuf );
	    return conversionBuf.toString();
	} catch( IOException ex ) {
	    ex.printStackTrace();
	    return null;
	}
    }

    */
}

// -------------------- Private implementation --------------------



/**
 * 
 */
final class  ReadConvertor extends InputStreamReader {
    
    // Has a private, internal byte[8192]
    
    /** Create a converter.
     */
    public ReadConvertor( IntermediateInputStream in, String enc )
	throws UnsupportedEncodingException
    {
	super( in, enc );
    }
    
    /** Overriden - will do nothing but reset internal state.
     */
    public  final void close() throws IOException {
	// NOTHING
	// Calling super.close() would reset out and cb.
    }
    
    public  final int read(char cbuf[], int off, int len)
	throws IOException
    {
	// will do the conversion and call write on the output stream
	return super.read( cbuf, off, len );
    }
    
    /** Reset the buffer
     */
    public  final void recycle() {
    }
}


/** Special output stream where close() is overriden, so super.close()
    is never called.
    
    This allows recycling. It can also be disabled, so callbacks will
    not be called if recycling the converter and if data was not flushed.
*/
final class IntermediateInputStream extends InputStream {
    byte buf[];
    int pos;
    int len;
    int end;
    
    public IntermediateInputStream() {
    }
    
    public  final void close() throws IOException {
	// shouldn't be called - we filter it out in writer
	throw new IOException("close() called - shouldn't happen ");
    }
    
    public  final  int read(byte cbuf[], int off, int len) throws IOException {
	if( pos >= end ) return -1;
	if (pos + len > end) {
	    len = end - pos;
	}
	if (len <= 0) {
	    return 0;
	}
	System.arraycopy(buf, pos, cbuf, off, len);
	pos += len;
	return len;
    }
    
    public  final int read() throws IOException {
	return (pos < end ) ? (buf[pos++] & 0xff) : -1;
    }

    // -------------------- Internal methods --------------------

    void setBuffer( byte b[], int p, int l ) {
	buf=b;
	pos=p;
	len=l;
	end=pos+len;
    }

    void setByteChunk( ByteChunk mb ) {
	buf=mb.getBytes();
	pos=mb.getStart();
	len=mb.getLength();
	end=pos+len;
    }

}
