 

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

package org.apache.coyote;

import java.io.IOException;
import java.util.HashMap;

import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.buf.UDecoder;

import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.http.Parameters;
import org.apache.tomcat.util.http.ContentType;
import org.apache.tomcat.util.http.Cookies;

/**
 * This is a low-level, efficient representation of a server request. Most 
 * fields are GC-free, expensive operations are delayed until the  user code 
 * needs the information.
 *
 * Processing is delegated to modules, using a hook mechanism.
 * 
 * This class is not intended for user code - it is used internally by tomcat
 * for processing the request in the most efficient way. Users ( servlets ) can
 * access the information using a facade, which provides the high-level view
 * of the request.
 *
 * For lazy evaluation, the request uses the getInfo() hook. The following ids
 * are defined:
 * <ul>
 *  <li>req.encoding - returns the request encoding
 *  <li>req.attribute - returns a module-specific attribute ( like SSL keys, etc ).
 * </ul>
 *
 * Tomcat defines a number of attributes:
 * <ul>
 *   <li>"org.apache.tomcat.request" - allows access to the low-level
 *       request object in trusted applications 
 * </ul>
 *
 * @author James Duncan Davidson [duncan@eng.sun.com]
 * @author James Todd [gonzo@eng.sun.com]
 * @author Jason Hunter [jch@eng.sun.com]
 * @author Harish Prabandham
 * @author Alex Cruikshank [alex@epitonic.com]
 * @author Hans Bergsten [hans@gefionsoftware.com]
 * @author Costin Manolache
 * @author Remy Maucherat
 */
public final class Request {


    // ----------------------------------------------------------- Constructors


    public Request() {

        parameters.setQuery(queryMB);
        parameters.setURLDecoder(urlDecoder);
        parameters.setHeaders(headers);

        schemeMB.setString("http");
        methodMB.setString("GET");
        /* SJSWS 6376484
        uriMB.setString("/");
        */
        queryMB.setString("");
        protoMB.setString("HTTP/1.0");

    }


    // ----------------------------------------------------- Instance Variables


    private int serverPort = -1;
    private MessageBytes serverNameMB = new MessageBytes();

    private String localHost;
    
    private int remotePort;
    private int localPort;

    private MessageBytes schemeMB = new MessageBytes();

    private MessageBytes methodMB = new MessageBytes();
    private MessageBytes unparsedURIMB = new MessageBytes();
    private MessageBytes uriMB = new MessageBytes();
    private MessageBytes decodedUriMB = new MessageBytes();
    private MessageBytes queryMB = new MessageBytes();
    private MessageBytes protoMB = new MessageBytes();

    // remote address/host
    private MessageBytes remoteAddrMB = new MessageBytes();
    private MessageBytes localNameMB = new MessageBytes();
    private MessageBytes remoteHostMB = new MessageBytes();
    private MessageBytes localAddrMB = new MessageBytes();
     
    private MimeHeaders headers = new MimeHeaders();

    private MessageBytes instanceId = new MessageBytes();

    /**
     * Notes.
     */
    private NotesManagerImpl notesManager = new NotesManagerImpl();


    /**
     * Associated input buffer.
     */
    private InputBuffer inputBuffer = null;


    /**
     * URL decoder.
     */
    private UDecoder urlDecoder = new UDecoder();


    /**
     * HTTP specific fields. (remove them ?)
     */
    private long contentLength = -1;
    private MessageBytes contentTypeMB = null;
    private String charEncoding = null;
    private boolean charEncodingParsed = false;
    private Cookies cookies = new Cookies(headers);
    private Parameters parameters = new Parameters();

    private MessageBytes remoteUser=new MessageBytes();
    private MessageBytes authType=new MessageBytes();
    private HashMap attributes=new HashMap();

    private Response response;
    private ActionHook hook;

    private int bytesRead=0;
    // Time of the request - usefull to avoid repeated calls to System.currentTime
    private long startTime = 0L;

    private RequestInfo reqProcessorMX=new RequestInfo(this);
    // ------------------------------------------------------------- Properties


    /**
     * Get the instance id (or JVM route). Curently Ajp is sending it with each
     * request. In future this should be fixed, and sent only once ( or
     * 'negociated' at config time so both tomcat and apache share the same name.
     * 
     * @return the instance id
     */
    public MessageBytes instanceId() {
        return instanceId;
    }


    public MimeHeaders getMimeHeaders() {
        return headers;
    }


    public UDecoder getURLDecoder() {
        return urlDecoder;
    }

    // -------------------- Request data --------------------


    public MessageBytes scheme() {
        return schemeMB;
    }
    
    public MessageBytes method() {
        return methodMB;
    }
    
    public MessageBytes unparsedURI() {
        return unparsedURIMB;
    }

    public MessageBytes requestURI() {
        return uriMB;
    }

    public MessageBytes decodedURI() {
        return decodedUriMB;
    }

    public MessageBytes query() {
        return queryMB;
    }

    public MessageBytes queryString() {
        return queryMB;
    }

    public MessageBytes protocol() {
        return protoMB;
    }
    
    /** 
     * Return the buffer holding the server name, if
     * any. Use isNull() to check if there is no value
     * set.
     * This is the "virtual host", derived from the
     * Host: header.
     */
    public MessageBytes serverName() {
	return serverNameMB;
    }

    public int getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(int serverPort ) {
	this.serverPort=serverPort;
    }

    public MessageBytes remoteAddr() {
	return remoteAddrMB;
    }

    public MessageBytes remoteHost() {
	return remoteHostMB;
    }

    public MessageBytes localName() {
	return localNameMB;
    }    

    public MessageBytes localAddr() {
	return localAddrMB;
    }
    
    public String getLocalHost() {
	return localHost;
    }

    public void setLocalHost(String host) {
	this.localHost = host;
    }    
    
    public int getRemotePort(){
        return remotePort;
    }
        
    public void setRemotePort(int port){
        this.remotePort = port;
    }
    
    public int getLocalPort(){
        return localPort;
    }
        
    public void setLocalPort(int port){
        this.localPort = port;
    }

    // -------------------- encoding/type --------------------


    /**
     * Get the character encoding used for this request.
     */
    public String getCharacterEncoding() {

        if (charEncoding != null || charEncodingParsed) {
            return charEncoding;
        }

        charEncoding = ContentType.getCharsetFromContentType(getContentType());
        charEncodingParsed = true;

        return charEncoding;
    }


    public void setCharacterEncoding(String enc) {
	this.charEncoding = enc;
    }


    public void setContentLength(int len) {
	this.contentLength = len;
    }


    public int getContentLength() {
        long length = getContentLengthLong();
        
        if (length < Integer.MAX_VALUE) {
            return (int) length;
        }
        return -1;
    }
    
    public long getContentLengthLong() {
        if( contentLength > -1 ) return contentLength;

        MessageBytes clB = headers.getUniqueValue("content-length");
        contentLength = (clB == null || clB.isNull()) ? -1 : clB.getLong();

        return contentLength;
    }


    public String getContentType() {
        contentType();
        if ((contentTypeMB == null) || contentTypeMB.isNull()) 
            return null;
        return contentTypeMB.toString();
    }


    public void setContentType(String type) {
        contentTypeMB.setString(type);
    }


    public MessageBytes contentType() {
        if (contentTypeMB == null)
            contentTypeMB = headers.getValue("content-type");
        return contentTypeMB;
    }


    public void setContentType(MessageBytes mb) {
        contentTypeMB=mb;
    }


    public String getHeader(String name) {
        return headers.getHeader(name);
    }

    // -------------------- Associated response --------------------

    public Response getResponse() {
        return response;
    }

    public void setResponse( Response response ) {
        this.response=response;
        response.setRequest( this );
    }
    
    public void action(ActionCode actionCode, Object param) {
        if( hook==null && response!=null )
            hook=response.getHook();
        
        if (hook != null) {
            if( param==null ) 
                hook.action(actionCode, this);
            else
                hook.action(actionCode, param);
        }
    }


    // -------------------- Cookies --------------------


    public Cookies getCookies() {
	return cookies;
    }


    // -------------------- Parameters --------------------


    public Parameters getParameters() {
	return parameters;
    }


    // -------------------- Other attributes --------------------
    // We can use notes for most - need to discuss what is of general interest
    
    public void setAttribute( String name, Object o ) {
        attributes.put( name, o );
    }

    public HashMap getAttributes() {
        return attributes;
    }

    public Object getAttribute(String name ) {
        return attributes.get(name);
    }
    
    public MessageBytes getRemoteUser() {
        return remoteUser;
    }

    public MessageBytes getAuthType() {
        return authType;
    }

    // -------------------- Input Buffer --------------------


    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }


    public void setInputBuffer(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }


    /**
     * Read data from the input buffer and put it into a byte chunk.
     *
     * The buffer is owned by the protocol implementation - it will be reused
     * on the next read.
     * The Adapter must either process the data in place or copy it to a
     * separate buffer if it needs to hold it. In most cases this is done
     * during byte->char conversions or via InputStream. Unlike InputStream,
     * this interface allows the app to process data in place, without copy.
     */
    public int doRead(ByteChunk chunk) 
        throws IOException {
        int n = inputBuffer.doRead(chunk, this);
        if (n > 0) {
            bytesRead+=n;
        }
        return n;
    }


    // -------------------- debug --------------------

    public String toString() {
	return "R( " + requestURI().toString() + ")";
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    // -------------------- Per-Request "notes" --------------------


    public final void setNote(int pos, Object value) {
	notesManager.setNote(pos,value);
    }


    public final Object getNote(int pos) {
	return notesManager.getNote(pos);
    }
    

    public NotesManagerImpl getNotesManager() {
        return notesManager;
    }

    
    public void setNotesManager(NotesManagerImpl notesManager) {
        this.notesManager = notesManager;
    }   
    
    // -------------------- Recycling -------------------- 


    public void recycle() {
        bytesRead=0;

	contentLength = -1;
        contentTypeMB = null;
        charEncoding = null;
        charEncodingParsed = false;
        headers.recycle();
        serverNameMB.recycle();
        serverPort=-1;
        localPort = -1;
        remotePort = -1;

	cookies.recycle();
        parameters.recycle();

        unparsedURIMB.recycle();
        uriMB.recycle(); 
        decodedUriMB.recycle();
	queryMB.recycle();
	methodMB.recycle();
	protoMB.recycle();
	//remoteAddrMB.recycle();
	//remoteHostMB.recycle();

	// XXX Do we need such defaults ?
        schemeMB.recycle();
	methodMB.setString("GET");
        /* SJSWS 6376484
        uriMB.setString("/");
        */
        queryMB.setString("");
        protoMB.setString("HTTP/1.0");
        //remoteAddrMB.setString("127.0.0.1");
        //remoteHostMB.setString("localhost");

        instanceId.recycle();
        remoteUser.recycle();
        authType.recycle();
        attributes.clear();
    }

    // -------------------- Info  --------------------
    public void updateCounters() {
        reqProcessorMX.updateCounters();
    }

    public RequestInfo getRequestProcessor() {
        return reqProcessorMX;
    }

    public int getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(int bytesRead) {
        this.bytesRead = bytesRead;
    }
}
