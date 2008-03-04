

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

import java.text.*;
import java.util.*;
import java.io.Serializable;
import java.io.IOException;
import org.apache.tomcat.util.buf.*;

/**
 * Moved from ByteChunk - code to convert from UTF8 bytes to chars.
 * Not used in the current tomcat3.3 : the performance gain is not very
 * big if the String is created, only if we avoid that and work only
 * on char[]. Until than, it's better to be safe. ( I tested this code
 * with 2 and 3 bytes chars, and it works fine in xerces )
 * 
 * Cut from xerces' UTF8Reader.copyMultiByteCharData() 
 *
 * @author Costin Manolache
 * @author ( Xml-Xerces )
 */
public final class UTF8Decoder extends B2CConverter {
 
    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog(UTF8Decoder.class );

    // may have state !!
    
    public UTF8Decoder() {

    }
    
    public void recycle() {
    }

    public void convert(ByteChunk mb, CharChunk cb )
	throws IOException
    {
	int bytesOff=mb.getOffset();
	int bytesLen=mb.getLength();
	byte bytes[]=mb.getBytes();
	
	int j=bytesOff;
	int end=j+bytesLen;

	while( j< end ) {
	    int b0=0xff & bytes[j];

	    if( (b0 & 0x80) == 0 ) {
		cb.append((char)b0);
		j++;
		continue;
	    }
	    
	    // 2 byte ?
	    if( j++ >= end ) {
		// ok, just ignore - we could throw exception
		throw new IOException( "Conversion error - EOF " );
	    }
	    int b1=0xff & bytes[j];
	    
	    // ok, let's the fun begin - we're handling UTF8
	    if ((0xe0 & b0) == 0xc0) { // 110yyyyy 10xxxxxx (0x80 to 0x7ff)
		int ch = ((0x1f & b0)<<6) + (0x3f & b1);
		if(debug>0)
		    log("Convert " + b0 + " " + b1 + " " + ch + ((char)ch));
		
		cb.append((char)ch);
		j++;
		continue;
	    }
	    
	    if( j++ >= end ) 
		return ;
	    int b2=0xff & bytes[j];
	    
	    if( (b0 & 0xf0 ) == 0xe0 ) {
		if ((b0 == 0xED && b1 >= 0xA0) ||
		    (b0 == 0xEF && b1 == 0xBF && b2 >= 0xBE)) {
		    if(debug>0)
			log("Error " + b0 + " " + b1+ " " + b2 );

		    throw new IOException( "Conversion error 2"); 
		}

		int ch = ((0x0f & b0)<<12) + ((0x3f & b1)<<6) + (0x3f & b2);
		cb.append((char)ch);
		if(debug>0)
		    log("Convert " + b0 + " " + b1+ " " + b2 + " " + ch +
			((char)ch));
		j++;
		continue;
	    }

	    if( j++ >= end ) 
		return ;
	    int b3=0xff & bytes[j];

	    if (( 0xf8 & b0 ) == 0xf0 ) {
		if (b0 > 0xF4 || (b0 == 0xF4 && b1 >= 0x90)) {
		    if(debug>0)
			log("Convert " + b0 + " " + b1+ " " + b2 + " " + b3);
		    throw new IOException( "Conversion error ");
		}
		int ch = ((0x0f & b0)<<18) + ((0x3f & b1)<<12) +
		    ((0x3f & b2)<<6) + (0x3f & b3);

		if(debug>0)
		    log("Convert " + b0 + " " + b1+ " " + b2 + " " + b3 + " " +
			ch + ((char)ch));

		if (ch < 0x10000) {
		    cb.append( (char)ch );
		} else {
		    cb.append((char)(((ch-0x00010000)>>10)+
						   0xd800));
		    cb.append((char)(((ch-0x00010000)&0x3ff)+
						   0xdc00));
		}
		j++;
		continue;
	    } else {
		// XXX Throw conversion exception !!!
		if(debug>0)
		    log("Convert " + b0 + " " + b1+ " " + b2 + " " + b3);
		throw new IOException( "Conversion error 4" );
	    }
	}
    }

    private static int debug=1;
    void log(String s ) {
        if (log.isDebugEnabled())
	    log.debug("UTF8Decoder: " + s );
    }
    
}
