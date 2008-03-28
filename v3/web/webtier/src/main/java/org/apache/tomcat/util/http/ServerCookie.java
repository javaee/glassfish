

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
package org.apache.tomcat.util.http;

import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.buf.DateTool;
import java.text.*;
import java.io.*;
import java.util.*;

// START OF IASRI 4830338
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
// END OF IASRI 4830338


/**
 *  Server-side cookie representation.
 *   Allows recycling and uses MessageBytes as low-level
 *  representation ( and thus the byte-> char conversion can be delayed
 *  until we know the charset ).
 *
 *  Tomcat.core uses this recyclable object to represent cookies,
 *  and the facade will convert it to the external representation.
 */
public class ServerCookie implements Serializable {

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog(ServerCookie.class );

    // START PWC 6392327
    /**
     * The string sent as the value of the cookie in the Set-Cookie header
     * for a Cookie whose value is null.
     */
    private static final String NULL_VALUE = "null";
    // END PWC 6392327

    private static final String ANCIENT_DATE =
        DateTool.formatOldCookie(new Date(10000));

    private MessageBytes name=MessageBytes.newInstance();
    private MessageBytes value=MessageBytes.newInstance();

    private MessageBytes comment=MessageBytes.newInstance();    // ;Comment=VALUE
    private MessageBytes domain=MessageBytes.newInstance();    // ;Domain=VALUE ...

    private int maxAge = -1;	// ;Max-Age=VALUE
				// ;Discard ... implied by maxAge < 0
    // RFC2109: maxAge=0 will end a session
    private MessageBytes path=MessageBytes.newInstance();	// ;Path=VALUE .
    private boolean secure;	// ;Secure
    private int version = 0;	// ;Version=1

    //XXX CommentURL, Port -> use notes ?
    
    public ServerCookie() {

    }

    public void recycle() {
        path.recycle();
    	name.recycle();
    	value.recycle();
    	comment.recycle();
    	maxAge=-1;
    	path.recycle();
        domain.recycle();
    	version=0;
    	secure=false;
    }

    public MessageBytes getComment() {
	return comment;
    }

    public MessageBytes getDomain() {
	return domain;
    }

    public void setMaxAge(int expiry) {
	maxAge = expiry;
    }

    public int getMaxAge() {
	return maxAge;
    }


    public MessageBytes getPath() {
	return path;
    }

    public void setSecure(boolean flag) {
	secure = flag;
    }

    public boolean getSecure() {
	return secure;
    }

    public MessageBytes getName() {
	return name;
    }

    public MessageBytes getValue() {
	return value;
    }

    public int getVersion() {
	return version;
    }


    public void setVersion(int v) {
	version = v;
    }


    // -------------------- utils --------------------

    public String toString() {
	return "Cookie " + getName() + "=" + getValue() + " ; "
	    + getVersion() + " " + getPath() + " " + getDomain();
    }
    
    // Note -- disabled for now to allow full Netscape compatibility
    // from RFC 2068, token special case characters
    //
    // private static final String tspecials = "()<>@,;:\\\"/[]?={} \t";
    private static final String tspecials = ",; ";

    /*
     * Tests a string and returns true if the string counts as a
     * reserved token in the Java language.
     *
     * @param value		the <code>String</code> to be tested
     *
     * @return			<code>true</code> if the <code>String</code> is
     *				a reserved token; <code>false</code>
     *				if it is not
     */
    public static boolean isToken(String value) {
	if( value==null) return true;
	int len = value.length();

	for (int i = 0; i < len; i++) {
	    char c = value.charAt(i);

	    if (c < 0x20 || c >= 0x7f || tspecials.indexOf(c) != -1)
		return false;
	}
	return true;
    }

    public static boolean checkName( String name ) {
	if (!isToken(name)
		|| name.equalsIgnoreCase("Comment")	// rfc2019
		|| name.equalsIgnoreCase("Discard")	// 2019++
		|| name.equalsIgnoreCase("Domain")
		|| name.equalsIgnoreCase("Expires")	// (old cookies)
		|| name.equalsIgnoreCase("Max-Age")	// rfc2019
		|| name.equalsIgnoreCase("Path")
		|| name.equalsIgnoreCase("Secure")
		|| name.equalsIgnoreCase("Version")
	    ) {
	    return false;
	}
	return true;
    }

    // -------------------- Cookie parsing tools

    
    /** Return the header name to set the cookie, based on cookie
     *  version
     */
    public String getCookieHeaderName() {
	return getCookieHeaderName(version);
    }

    /** Return the header name to set the cookie, based on cookie
     *  version
     */
    public static String getCookieHeaderName(int version) {
	if( dbg>0 ) log( (version==1) ? "Set-Cookie2" : "Set-Cookie");
        if (version == 1) {
	    // RFC2109
	    return "Set-Cookie";
	    // XXX RFC2965 is not standard yet, and Set-Cookie2
	    // is not supported by Netscape 4, 6, IE 3, 5 .
	    // It is supported by Lynx, and there is hope 
	    //	    return "Set-Cookie2";
        } else {
	    // Old Netscape
	    return "Set-Cookie";
        }
    }


    public static void appendCookieValue(StringBuffer buf,
                                         int version,
                                         String name,
                                         String value,
                                         String path,
                                         String domain,
                                         String comment,
                                         int maxAge,
                                         boolean isSecure) {
        appendCookieValue(buf, version, name, value, path, domain, comment,
                          maxAge, isSecure, false);
    }


    public static void appendCookieValue(StringBuffer buf,
                                         int version,
                                         String name,
                                         String value,
                                         String path,
                                         String domain,
                                         String comment,
                                         int maxAge,
                                         boolean isSecure,
                                         boolean encode) {

        // this part is the same for all cookies
        if (encode) {
            try {
                buf.append(URLEncoder.encode(name, "UTF-8"));
                buf.append("=");
                /* PWC 6392327
                maybeQuote(version, buf, URLEncoder.encode(value, "UTF-8"));
                */
                // START PWC 6392327
                if (value != null) {
                    maybeQuote(version, buf,
                               URLEncoder.encode(value, "UTF-8"));
                } else {
                    maybeQuote(version, buf,
                               URLEncoder.encode(ServerCookie.NULL_VALUE,
                                                 "UTF-8"));
                }
                // END PWC 6392327
            } catch (UnsupportedEncodingException e) {
                buf.append(URLEncoder.encode(name));
                buf.append("=");
                /* PWC 6392327
                maybeQuote(version, buf, URLEncoder.encode(value));
                */
                // START PWC 6392327
                if (value != null) {
                    maybeQuote(version, buf,
                               URLEncoder.encode(value));
                } else {
                    maybeQuote(version, buf,
                               URLEncoder.encode(ServerCookie.NULL_VALUE));
                }
                // END PWC 6392327
            }
        } else {
            buf.append( name );
            buf.append("=");
            /* PWC 6392327
            maybeQuote(version, buf, value);
            */
            // START PWC 6392327
            if (value != null) {
                maybeQuote(version, buf, value);
            } else {
                maybeQuote(version, buf, ServerCookie.NULL_VALUE);
            }
            // END PWC 6392327
        }

	// XXX Netscape cookie: "; "
 	// add version 1 specific information
	if (version == 1) {
	    // Version=1 ... required
	    buf.append ("; Version=1");

	    // Comment=comment
	    if (comment!=null) {
                buf.append ("; Comment=");
                if (encode) {
                    try {
                        maybeQuote(version, buf,
                                   URLEncoder.encode(comment, "UTF-8"));
                    } catch(UnsupportedEncodingException e) {
                        maybeQuote(version, buf,
                                   URLEncoder.encode(comment));
                    }
	        } else {
                    maybeQuote (version, buf, comment);
                }
            }
        }
	
	// add domain information, if present

	if (domain!=null) {
	    buf.append("; Domain=");
	    maybeQuote (version, buf, domain);
	}

	// Max-Age=secs/Discard ... or use old "Expires" format
	if (maxAge >= 0) {
	    if (version == 0) {
		// XXX XXX XXX We need to send both, for
		// interoperatibility (long word )
		buf.append ("; Expires=");
		// Wdy, DD-Mon-YY HH:MM:SS GMT ( Expires netscape format )
		// To expire we need to set the time back in future
		// ( pfrieden@dChain.com )
                if (maxAge == 0)
		    buf.append( ANCIENT_DATE );
		else
                    DateTool.formatOldCookie
                        (new Date( System.currentTimeMillis() +
                                   maxAge *1000L), buf,
                         new FieldPosition(0));

	    } else {
		buf.append ("; Max-Age=");
		buf.append (maxAge);
	    }
	}

	// Path=path
	if (path!=null) {
	    buf.append ("; Path=");
	    maybeQuote (version, buf, path);
	}

	// Secure
	if (isSecure) {
	  buf.append ("; Secure");
	}
	
	
    }


    // START OF IASRI 4830338
    public static void appendEncodedCookieValue( StringBuffer buf,
					  int version,
					  String name,
					  String value,
					  String path,
					  String domain,
					  String comment,
					  int maxAge,
					  boolean isSecure) {
        appendCookieValue(buf, version, name, value, path, domain, comment,
                          maxAge, isSecure, true);
    }
    // END OF IASRI 4830338


    public static void maybeQuote (int version, StringBuffer buf,
                                   String value)
    {
        // special case - a \n or \r  shouldn't happen in any case
        if (isToken(value)) {
            buf.append(value);
        } else {
            buf.append('"');
            buf.append(escapeDoubleQuotes(value));
            buf.append('"');
        }
    }

    // log
    static final int dbg=1;
    public static void log(String s ) {
        if (log.isDebugEnabled())
	    log.debug("ServerCookie: " + s);
    }

    /**
     * Escapes any double quotes in the given string.
     *
     * @param s the input string
     *
     * @return The (possibly) escaped string
     */
    private static String escapeDoubleQuotes(String s) {

        if (s == null || s.length() == 0 || s.indexOf('"') == -1) {
            return s;
        }

        StringBuffer b = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"')
                b.append('\\').append('"');
            else
                b.append(c);
        }

        return b.toString();
    }

}

