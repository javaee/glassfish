

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
import java.util.BitSet;
import java.io.*;

/** Efficient implementation for encoders.
 *  This class is not thread safe - you need one encoder per thread.
 *  The encoder will save and recycle the internal objects, avoiding
 *  garbage.
 * 
 *  You can add extra characters that you want preserved, for example
 *  while encoding a URL you can add "/".
 *
 *  @author Costin Manolache
 */
public final class UEncoder {

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog(UEncoder.class );

    // Not static - the set may differ ( it's better than adding
    // an extra check for "/", "+", etc
    private BitSet safeChars=null;
    private C2BConverter c2b=null;
    private ByteChunk bb=null;

    private String encoding="UTF8";
    private static final int debug=0;
    
    public UEncoder() {
	initSafeChars();
    }

    public void setEncoding( String s ) {
	encoding=s;
    }

    public void addSafeCharacter( char c ) {
	safeChars.set( c );
    }


    /** URL Encode string, using a specified encoding.
     *  @param s string to be encoded
     *  @param enc character encoding, for chars >%80 ( use UTF8 if not set,
     *         as recommended in RFCs)
     *  @param reserved extra characters to preserve ( "/" - if s is a URL )
     */
    public void urlEncode( Writer buf, String s )
	throws IOException
    {
	if( c2b==null ) {
	    bb=new ByteChunk(16); // small enough.
	    c2b=C2BConverter.getInstance( bb, encoding );
	}

	for (int i = 0; i < s.length(); i++) {
	    int c = (int) s.charAt(i);
	    if( safeChars.get( c ) ) {
		if( debug > 0 ) log("Safe: " + (char)c);
		buf.write((char)c);
	    } else {
		if( debug > 0 ) log("Unsafe:  " + (char)c);
		c2b.convert( (char)c );
		
		// "surrogate" - UTF is _not_ 16 bit, but 21 !!!!
		// ( while UCS is 31 ). Amazing...
		if (c >= 0xD800 && c <= 0xDBFF) {
		    if ( (i+1) < s.length()) {
			int d = (int) s.charAt(i+1);
			if (d >= 0xDC00 && d <= 0xDFFF) {
			    if( debug > 0 ) log("Unsafe:  " + c);
			    c2b.convert( (char)d);
			    i++;
			}
		    }
		}

		urlEncode( buf, bb.getBuffer(), bb.getOffset(),
			   bb.getLength() );
		bb.recycle();
	    }
	}
    }

    /**
     */
    public void urlEncode( Writer buf, byte bytes[], int off, int len)
	throws IOException
    {
	for( int j=off; j< len; j++ ) {
	    buf.write( '%' );
	    char ch = Character.forDigit((bytes[j] >> 4) & 0xF, 16);
	    if( debug > 0 ) log("Encode:  " + ch);
	    buf.write(ch);
	    ch = Character.forDigit(bytes[j] & 0xF, 16);
	    if( debug > 0 ) log("Encode:  " + ch);
	    buf.write(ch);
	}
    }
    
    /**
     * Utility funtion to re-encode the URL.
     * Still has problems with charset, since UEncoder mostly
     * ignores it.
     */
    public String encodeURL(String uri) {
	String outUri=null;
	try {
	    // XXX optimize - recycle, etc
	    CharArrayWriter out = new CharArrayWriter();
	    urlEncode(out, uri);
	    outUri=out.toString();
	} catch (IOException iex) {
	}
	return outUri;
    }
    

    // -------------------- Internal implementation --------------------
    
    // 
    private void init() {
	
    }
    
    private void initSafeChars() {
	safeChars=new BitSet(128);
	int i;
	for (i = 'a'; i <= 'z'; i++) {
	    safeChars.set(i);
	}
	for (i = 'A'; i <= 'Z'; i++) {
	    safeChars.set(i);
	}
	for (i = '0'; i <= '9'; i++) {
	    safeChars.set(i);
	}
	//safe
	safeChars.set('$');
	safeChars.set('-');
	safeChars.set('_');
	safeChars.set('.');

	// Dangerous: someone may treat this as " "
	// RFC1738 does allow it, it's not reserved
	//    safeChars.set('+');
	//extra
	safeChars.set('!');
	safeChars.set('*');
	safeChars.set('\'');
	safeChars.set('(');
	safeChars.set(')');
	safeChars.set(',');	
    }

    private static void log( String s ) {
        if (log.isDebugEnabled())
	    log.debug("Encoder: " + s );
    }
}
