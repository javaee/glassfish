

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

package org.apache.coyote.http11;

import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.InetAddress;
// START SJSAS 6415934
import org.apache.catalina.util.ServerInfo;
// END SJSAS 6415934
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.buf.Ascii;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.threads.ThreadPool;
import org.apache.tomcat.util.threads.ThreadWithAttributes;

import org.apache.coyote.ActionHook;
import org.apache.coyote.ActionCode;
import org.apache.coyote.Adapter;
import org.apache.coyote.Processor;
import org.apache.coyote.Request;
import org.apache.coyote.RequestInfo;
import org.apache.coyote.Response;

import org.apache.coyote.http11.filters.ChunkedInputFilter;
import org.apache.coyote.http11.filters.ChunkedOutputFilter;
//import org.apache.coyote.http11.filters.GzipInputFilter;
import org.apache.coyote.http11.filters.GzipOutputFilter;
import org.apache.coyote.http11.filters.IdentityInputFilter;
import org.apache.coyote.http11.filters.IdentityOutputFilter;
import org.apache.coyote.http11.filters.VoidInputFilter;
import org.apache.coyote.http11.filters.VoidOutputFilter;
import org.apache.coyote.http11.filters.BufferedInputFilter;


/**
 * Processes HTTP requests.
 * 
 * @author Remy Maucherat
 */
public class Http11Processor implements Processor, ActionHook {


    // ----------------------------------------------------------- Constructors


    /**
     * Default constructor.
     */
    public Http11Processor() {
        this(Constants.DEFAULT_HTTP_HEADER_BUFFER_SIZE);
    }


    public Http11Processor(int headerBufferSize) {

        request = new Request();
        // START OF SJSAS PE 8.1 6172948
        //inputBuffer = new InternalInputBuffer(request, headerBufferSize);
        inputBuffer = new InternalInputBuffer(request, requestBufferSize);
        // END OF SJSAS PE 8.1 6172948

        request.setInputBuffer(inputBuffer);

        response = new Response();
        response.setHook(this);
        outputBuffer = new InternalOutputBuffer(response, headerBufferSize);
        response.setOutputBuffer(outputBuffer);
        request.setResponse(response);

        initializeFilters();


        // START OF SJSAS PE 8.1 5036984
        if (System.getProperty(USE_KEEP_ALIVE) != null) {
            useKeepAliveAlgorithm = 
                        Boolean.valueOf(
                            System.getProperty(USE_KEEP_ALIVE)).booleanValue();

            if (!useKeepAliveAlgorithm)
                log.warn("Keep Alive algorith will no be used"); 
        }
        // END OF SJSAS PE 8.1 5036984

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Associated adapter.
     */
    protected Adapter adapter = null;


    /**
     * Request object.
     */
    protected Request request = null;


    /**
     * Response object.
     */
    protected Response response = null;


    /**
     * Input.
     */
    protected InternalInputBuffer inputBuffer = null;


    /**
     * Output.
     */
    protected InternalOutputBuffer outputBuffer = null;


    /**
     * State flag.
     */
    protected boolean started = false;


    /**
     * Error flag.
     */
    protected boolean error = false;


    /**
     * Keep-alive.
     */
    protected boolean keepAlive = true;


    /**
     * HTTP/1.1 flag.
     */
    protected boolean http11 = true;


    /**
     * HTTP/0.9 flag.
     */
    protected boolean http09 = false;


    /**
     * Content delimitator for the request (if false, the connection will
     * be closed at the end of the request).
     */
    protected boolean contentDelimitation = true;


    /**
     * List of restricted user agents.
     */
    protected String[] restrictedUserAgents = null;


    /**
     * Logger.
     */
    protected static final com.sun.org.apache.commons.logging.Log log 
        = com.sun.org.apache.commons.logging.LogFactory.getLog(Http11Processor.class);


    /**
     * Maximum number of Keep-Alive requests to honor.
     */
    protected int maxKeepAliveRequests = -1;


    /**
     * SSL information.
     */
    protected SSLSupport sslSupport;


    /**
     * Socket associated with the current connection.
     */
    protected Socket socket;


    /**
     * Remote Address associated with the current connection.
     */
    protected String remoteAddr = null;


    /**
     * Remote Host associated with the current connection.
     */
    protected String remoteHost = null;


    /**
     * Local Host associated with the current connection.
     */
    protected String localName = null;


    /**
     * Local port to which the socket is connected
     */
    protected int localPort = -1;

    
    /**
     * Remote port to which the socket is connected
     */
    protected int remotePort = -1;
    
    
    /**
     * The local Host address.
     */
    protected String localAddr = null; 
    
    /**
     * Maximum timeout on uploads.
     */
    protected int timeout = 300000;   // 5 minutes as in Apache HTTPD server


    /**
     * Flag to disable setting a different time-out on uploads.
     */
    protected boolean disableUploadTimeout = false;


    /**
     * Allowed compression level.
     */
    protected int compressionLevel = 0;


    /**
     * Minimum contentsize to make compression.
     */
    protected int compressionMinSize = 2048;


    /**
     * Max post size.
     */
    protected int maxPostSize = 2 * 1024 * 1024;


    /**
     * List of user agents to not use gzip with
     */
    protected String[] noCompressionUserAgents = null;


    /**
     * List of MIMES which could be gzipped
     */
    protected String[] compressableMimeTypes = { "text/html", "text/xml", "text/plain" };


    /**
     * Host name (used to avoid useless B2C conversion on the host name).
     */
    protected char[] hostNameC = new char[0];

    protected ThreadPool threadPool;


    // START OF SJSAS PE 8.1 5036984
    private final static String USE_KEEP_ALIVE =
                "com.sun.enterprise.web.connector.coyote.useKeepAliveAlgorithm";

    private boolean useKeepAliveAlgorithm = true;
    // END OF SJSAS PE 8.1 5036984
    
    
    // START OF SJSAS PE 8.1 6172948
    /**
     * The input request buffer size.
     */
    private int requestBufferSize = 4096;
    // END OF SJSAS PE 8.1 6172948

    // ------------------------------------------------------------- Properties


    /**
     * Return compression level.
     */
    public String getCompression() {
        switch (compressionLevel) {
        case 0:
            return "off";
        case 1:
            return "on";
        case 2:
            return "force";
        }
        return "off";
    }


    /**
     * Set compression level.
     */
    public void setCompression(String compression) {
        if (compression.equals("on")) {
            this.compressionLevel = 1;
        } else if (compression.equals("force")) {
            this.compressionLevel = 2;
        } else if (compression.equals("off")) {
            this.compressionLevel = 0;
        } else {
            try {
                // Try to parse compression as an int, which would give the
                // minimum compression size
                compressionMinSize = Integer.parseInt(compression);
                this.compressionLevel = 1;
            } catch (Exception e) {
                this.compressionLevel = 0;
            }
        }
    }

    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    /**
     * Add user-agent for which gzip compression didn't works
     * The user agent String given will be exactly matched
     * to the user-agent header submitted by the client.
     * 
     * @param userAgent user-agent string
     */
    public void addNoCompressionUserAgent(String userAgent) {
    	addStringArray(noCompressionUserAgents, userAgent);
    }


    /**
     * Set no compression user agent list (this method is best when used with 
     * a large number of connectors, where it would be better to have all of 
     * them referenced a single array).
     */
    public void setNoCompressionUserAgents(String[] noCompressionUserAgents) {
        this.noCompressionUserAgents = noCompressionUserAgents;
    }


    /**
     * Return the list of no compression user agents.
     */
    public String[] findNoCompressionUserAgents() {
        return (noCompressionUserAgents);
    }


    /**
     * Add a mime-type which will be compressable
     * The mime-type String will be exactly matched
     * in the response mime-type header .
     * 
     * @param userAgent user-agent string
     */
    public void addCompressableMimeType(String mimeType) {
    	addStringArray(compressableMimeTypes, mimeType);
    }


    /**
     * Set compressable mime-type list (this method is best when used with 
     * a large number of connectors, where it would be better to have all of 
     * them referenced a single array).
     */
    public void setCompressableMimeType(String[] compressableMimeTypes) {
        this.compressableMimeTypes = compressableMimeTypes;
    }


    /**
     * Return the list of restricted user agents.
     */
    public String[] findCompressableMimeTypes() {
        return (compressableMimeTypes);
    }



    // --------------------------------------------------------- Public Methods


    /**
     * Add input or output filter.
     * 
     * @param className class name of the filter
     */
    protected void addFilter(String className) {
        try {
            Class clazz = Class.forName(className);
            Object obj = clazz.newInstance();
            if (obj instanceof InputFilter) {
                inputBuffer.addFilter((InputFilter) obj);
            } else if (obj instanceof OutputFilter) {
                outputBuffer.addFilter((OutputFilter) obj);
            } else {
                // Not a valid filter: log and ignore
            }
        } catch (Exception e) {
            // Log and ignore
        }
    }


    /**
     * General use method
     * 
     * @param sArray the StringArray 
     * @param value string
     */
    private void addStringArray(String sArray[], String value) {
        if (sArray == null)
            sArray = new String[0];
        String[] results = new String[sArray.length + 1];
        for (int i = 0; i < sArray.length; i++)
            results[i] = sArray[i];
        results[sArray.length] = value;
        sArray = results;
    }

    /**
     * General use method
     * 
     * @param sArray the StringArray 
     * @param value string
     */
    private boolean inStringArray(String sArray[], String value) {
        for (int i = 0; i < sArray.length; i++) {
            if (sArray[i].equals(value)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Add restricted user-agent (which will downgrade the connector 
     * to HTTP/1.0 mode). The user agent String given will be exactly matched
     * to the user-agent header submitted by the client.
     * 
     * @param userAgent user-agent string
     */
    public void addRestrictedUserAgent(String userAgent) {
    	addStringArray(restrictedUserAgents, userAgent);
    }


    /**
     * Set restricted user agent list (this method is best when used with 
     * a large number of connectors, where it would be better to have all of 
     * them referenced a single array).
     */
    public void setRestrictedUserAgents(String[] restrictedUserAgents) {
        this.restrictedUserAgents = restrictedUserAgents;
    }


    /**
     * Return the list of restricted user agents.
     */
    public String[] findRestrictedUserAgents() {
        return (restrictedUserAgents);
    }


    /**
     * Set the maximum number of Keep-Alive requests to honor.
     * This is to safeguard from DoS attacks.  Setting to a negative
     * value disables the check.
     */
    public void setMaxKeepAliveRequests(int mkar) {
        maxKeepAliveRequests = mkar;
    }


    /**
     * Return the number of Keep-Alive requests that we will honor.
     */
    public int getMaxKeepAliveRequests() {
        return maxKeepAliveRequests;
    }


    /**
     * Set the maximum size of a POST which will be buffered in SSL mode.
     */
    public void setMaxPostSize(int mps) {
        maxPostSize = mps;
    }


    /**
     * Return the maximum size of a POST which will be buffered in SSL mode.
     */
    public int getMaxPostSize() {
        return maxPostSize;
    }


    /**
     * Set the SSL information for this HTTP connection.
     */
    public void setSSLSupport(SSLSupport sslSupport) {
        this.sslSupport = sslSupport;
    }


    /**
     * Set the socket associated with this HTTP connection.
     */
    public void setSocket(Socket socket)
        throws IOException {
        this.socket = socket;
    }

    /**
     * Set the flag to control upload time-outs.
     */
    public void setDisableUploadTimeout(boolean isDisabled) {
        disableUploadTimeout = isDisabled;
    }

    /**
     * Get the flag that controls upload time-outs.
     */
    public boolean getDisableUploadTimeout() {
        return disableUploadTimeout;
    }

    /**
     * Set the upload timeout.
     */
    public void setTimeout( int timeouts ) {
        timeout = timeouts ;
    }

    /**
     * Get the upload timeout.
     */
    public int getTimeout() {
        return timeout;
    }

    /** Get the request associated with this processor.
     *
     * @return
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Process pipelined HTTP requests using the specified input and output
     * streams.
     * 
     * @param input stream from which the HTTP requests will be read
     * @param output stream which will be used to output the HTTP
     * responses
     * @throws IOException error during an I/O operation
     */    
    // START OF SJSAS 6231069
    //  public void process(InputStream input, OutputStream output)
    public boolean process(InputStream input, OutputStream output)
        throws Exception {
    // END OF SJSAS 6231069
        ThreadWithAttributes thrA=
                (ThreadWithAttributes)Thread.currentThread();
        RequestInfo rp = request.getRequestProcessor();
        thrA.setCurrentStage(threadPool, "parsing http request");
        rp.setStage(org.apache.coyote.Constants.STAGE_PARSE);

        // Set the remote address
        remoteAddr = null;
        remoteHost = null;
        localAddr = null;
        remotePort = -1;

        // Setting up the I/O
        inputBuffer.setInputStream(input);
        outputBuffer.setOutputStream(output);

        // Error flag
        error = false;
        keepAlive = true;

        int keepAliveLeft = maxKeepAliveRequests;
        int soTimeout = socket.getSoTimeout();

        // START OF SJSAS PE 8.1 5036984
        if ( useKeepAliveAlgorithm ) {
        // END OF SJSAS PE 8.1 5036984
            float threadRatio = 
                (float) threadPool.getCurrentThreadsBusy() 
                / (float) threadPool.getMaxThreads();
            if ((threadRatio > 0.33) && (threadRatio <= 0.66)) {
                soTimeout = soTimeout / 2;
            } else if (threadRatio > 0.66) {
                soTimeout = soTimeout / 5;
                keepAliveLeft = 1;
            }
        // START OF SJSAS PE 8.1 5036984
        }
        // END OF SJSAS PE 8.1 5036984

        boolean keptAlive = false;

        while (started && !error && keepAlive) {
            try {
                if( !disableUploadTimeout && keptAlive && soTimeout > 0 ) {
                    socket.setSoTimeout(soTimeout);
                }
                inputBuffer.parseRequestLine();
                request.setStartTime(System.currentTimeMillis());
                thrA.setParam( threadPool, request.requestURI() );
                keptAlive = true;
                if (!disableUploadTimeout) {
                    socket.setSoTimeout(timeout);
                }
                inputBuffer.parseHeaders();
            } catch (IOException e) {
                error = true;
                break;
            } catch (Exception e) {
                log.debug("Error parsing HTTP request", e);
                // 400 - Bad Request
                response.setStatus(400);
                error = true;
            }

            // Setting up filters, and parse some request headers
            thrA.setCurrentStage(threadPool, "prepareRequest");
            rp.setStage(org.apache.coyote.Constants.STAGE_PREPARE);
            try {
                prepareRequest();
            } catch (Throwable t) {
                log.debug("Error preparing request", t);
                // 400 - Internal Server Error
                response.setStatus(400);
                error = true;
            }

            // START OF SJSAS PE 8.1 5036984
            if ( useKeepAliveAlgorithm ) {
            // END OF SJSAS PE 8.1 5036984
                if (maxKeepAliveRequests > 0 && --keepAliveLeft == 0)
                    keepAlive = false;
            // START OF SJSAS PE 8.1 5036984
            } else {
                keepAlive = false;
            }
            // END OF SJSAS PE 8.1 5036984

            // Process the request in the adapter
            if (!error) {
                try {
                    thrA.setCurrentStage(threadPool, "service");
                    rp.setStage(org.apache.coyote.Constants.STAGE_SERVICE);
                    adapter.service(request, response);
                    
                    // START GlassFish Issue 798
                    adapter.afterService(request, response);
                    // END GlassFish Issue 798
                    
                    // Handle when the response was committed before a serious
                    // error occurred.  Throwing a ServletException should both
                    // set the status to 500 and set the errorException.
                    // If we fail here, then the response is likely already 
                    // committed, so we can't try and set headers.
                    if(keepAlive && !error) { // Avoid checking twice.
                        error = response.getErrorException() != null ||
                                statusDropsConnection(response.getStatus());
                    }

                } catch (InterruptedIOException e) {
                    error = true;
                } catch (Throwable t) {
                    log.error("Error processing request", t);
                    // 500 - Internal Server Error
                    response.setStatus(500);
                    error = true;
                }
            }

            // Finish the handling of the request
            try {
                thrA.setCurrentStage(threadPool, "endRequestIB");
                rp.setStage(org.apache.coyote.Constants.STAGE_ENDINPUT);
                inputBuffer.endRequest();
            } catch (IOException e) {
                error = true;
            } catch (Throwable t) {
                log.error("Error finishing request", t);
                // 500 - Internal Server Error
                response.setStatus(500);
                error = true;
            }
            try {
                thrA.setCurrentStage(threadPool, "endRequestOB");
                rp.setStage(org.apache.coyote.Constants.STAGE_ENDOUTPUT);
                outputBuffer.endRequest();
            } catch (IOException e) {
                error = true;
            } catch (Throwable t) {
                log.error("Error finishing response", t);
                error = true;
            }

            thrA.setCurrentStage(threadPool, "ended");
            rp.setStage(org.apache.coyote.Constants.STAGE_KEEPALIVE);

            // Don't reset the param - we'll see it as ended. Next request
            // will reset it
            // thrA.setParam(null);
            // Next request
            inputBuffer.nextRequest();
            outputBuffer.nextRequest();

        }

        rp.setStage(org.apache.coyote.Constants.STAGE_ENDED);

        // Recycle
        inputBuffer.recycle();
        outputBuffer.recycle();

        // Recycle ssl info
        sslSupport = null;
        
        return true;
    }


    // ----------------------------------------------------- ActionHook Methods


    /**
     * Send an action to the connector.
     * 
     * @param actionCode Type of the action
     * @param param Action parameter
     */
    public void action(ActionCode actionCode, Object param) {

        if (actionCode == ActionCode.ACTION_COMMIT) {
            // Commit current response

            if (response.isCommitted())
                return;

            // Validate and write response headers
            prepareResponse();
            try {
                outputBuffer.commit();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_ACK) {

            // Acknowlege request

            // Send a 100 status back if it makes sense (response not committed
            // yet, and client specified an expectation for 100-continue)

            if (response.isCommitted())
                return;

            MessageBytes expectMB = 
                request.getMimeHeaders().getValue("expect");
            if ((expectMB != null)
                && (expectMB.indexOfIgnoreCase("100-continue", 0) != -1)) {
                try {
                    outputBuffer.sendAck();
                } catch (IOException e) {
                    // Set error flag
                    error = true;
                    response.setErrorException(e);
                }
            }

        } else if (actionCode == ActionCode.ACTION_CLOSE) {
            // Close

            // End the processing of the current request, and stop any further
            // transactions with the client

            try {
                outputBuffer.endRequest();
            } catch (IOException e) {
                // Set error flag
                error = true;
            }

        } else if (actionCode == ActionCode.ACTION_RESET) {

            // Reset response

            // Note: This must be called before the response is committed

            outputBuffer.reset();

        } else if (actionCode == ActionCode.ACTION_CUSTOM) {

            // Do nothing

        } else if (actionCode == ActionCode.ACTION_START) {

            started = true;

        } else if (actionCode == ActionCode.ACTION_STOP) {

            started = false;

        } else if (actionCode == ActionCode.ACTION_REQ_SSL_ATTRIBUTE ) {

            try {
                if (sslSupport != null) {
                    Object sslO = sslSupport.getCipherSuite();
                    if (sslO != null)
                        request.setAttribute
                            (SSLSupport.CIPHER_SUITE_KEY, sslO);
                    sslO = sslSupport.getPeerCertificateChain(false);
                    if (sslO != null)
                        request.setAttribute
                            (SSLSupport.CERTIFICATE_KEY, sslO);
                    sslO = sslSupport.getKeySize();
                    if (sslO != null)
                        request.setAttribute
                            (SSLSupport.KEY_SIZE_KEY, sslO);
                    sslO = sslSupport.getSessionId();
                    if (sslO != null)
                        request.setAttribute
                            (SSLSupport.SESSION_ID_KEY, sslO);
                }
            } catch (Exception e) {
                log.warn("Exception getting SSL attributes " ,e);
            }

        } else if (actionCode == ActionCode.ACTION_REQ_HOST_ADDR_ATTRIBUTE) {

            if ((remoteAddr == null) && (socket != null)) {
                InetAddress inetAddr = socket.getInetAddress();
                remoteAddr = inetAddr.getHostAddress();
                request.remoteAddr().setString(remoteAddr);
            }

        } else if (actionCode == ActionCode.ACTION_REQ_LOCAL_NAME_ATTRIBUTE) {

            if ((localName == null) && (socket != null)) {
                InetAddress inetAddr = socket.getLocalAddress();
                if (inetAddr != null) {
                    localName = inetAddr.getHostName();
                }
            }
            request.localName().setString(localName);

        } else if (actionCode == ActionCode.ACTION_REQ_HOST_ATTRIBUTE) {

            if ((remoteHost == null) && (socket != null)) {
                InetAddress inetAddr = socket.getInetAddress();
                if (inetAddr != null) {
                    remoteHost = inetAddr.getHostName();
                }
            }
            request.remoteHost().setString(remoteHost);

        } else if (actionCode == ActionCode.ACTION_REQ_LOCAL_ADDR_ATTRIBUTE) {

            if (localAddr == null)
               localAddr = socket.getLocalAddress().getHostAddress();

            request.localAddr().setString(localAddr);

        } else if (actionCode == ActionCode.ACTION_REQ_REMOTEPORT_ATTRIBUTE) {

            if ((remotePort == -1 ) && (socket !=null)) {
                remotePort = socket.getPort();
            }
            request.setRemotePort(remotePort);

        } else if (actionCode == ActionCode.ACTION_REQ_LOCALPORT_ATTRIBUTE) {

            if ((localPort == -1 ) && (socket !=null)) {
                localPort = socket.getLocalPort();
            }
            request.setLocalPort(localPort);

        } else if (actionCode == ActionCode.ACTION_REQ_SSL_CERTIFICATE) {
            if( sslSupport != null) {
                /*
                 * Consume and buffer the request body, so that it does not
                 * interfere with the client's handshake messages
                 */
                InputFilter[] inputFilters = inputBuffer.getFilters();
                ((BufferedInputFilter) inputFilters[Constants.BUFFERED_FILTER])
                    .setLimit(maxPostSize);
                inputBuffer.addActiveFilter
                    (inputFilters[Constants.BUFFERED_FILTER]);
                try {
                    Object sslO = sslSupport.getPeerCertificateChain(true);
                    if( sslO != null) {
                        request.setAttribute
                            (SSLSupport.CERTIFICATE_KEY, sslO);
                    }
                } catch (Exception e) {
                    log.warn("Exception getting SSL Cert",e);
                }
            }
        }

    }


    // ------------------------------------------------------ Connector Methods


    /**
     * Set the associated adapter.
     * 
     * @param adapter the new adapter
     */
    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
    }


    /**
     * Get the associated adapter.
     * 
     * @return the associated adapter
     */
    public Adapter getAdapter() {
        return adapter;
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * After reading the request headers, we have to setup the request filters.
     */
    protected void prepareRequest() {

        http11 = true;
        http09 = false;
        contentDelimitation = false;
        if (sslSupport != null) {
            request.scheme().setString("https");
        }
        MessageBytes protocolMB = request.protocol();
        if (protocolMB.equals(Constants.HTTP_11)) {
            http11 = true;
            protocolMB.setString(Constants.HTTP_11);
        } else if (protocolMB.equals(Constants.HTTP_10)) {
            http11 = false;
            keepAlive = false;
            protocolMB.setString(Constants.HTTP_10);
        } else if (protocolMB.equals("")) {
            // HTTP/0.9
            http09 = true;
            http11 = false;
            keepAlive = false;
        } else {
            // Unsupported protocol
            http11 = false;
            error = true;
            // Send 505; Unsupported HTTP version
            response.setStatus(505);
        }

        MessageBytes methodMB = request.method();
        if (methodMB.equals(Constants.GET)) {
            methodMB.setString(Constants.GET);
        } else if (methodMB.equals(Constants.POST)) {
            methodMB.setString(Constants.POST);
        }

        // Check connection header
        MessageBytes connectionValueMB = 
            request.getMimeHeaders().getValue("connection");
        if (connectionValueMB != null) {
            ByteChunk connectionValueBC = connectionValueMB.getByteChunk();
            if (findBytes(connectionValueBC, Constants.CLOSE_BYTES) != -1) {
                keepAlive = false;
            } else if (findBytes(connectionValueBC, 
                                 Constants.KEEPALIVE_BYTES) != -1) {
                keepAlive = true;
            }
        }

        // Check user-agent header
        if ((restrictedUserAgents != null) && ((http11) || (keepAlive))) {
            MessageBytes userAgentValueMB =  
                request.getMimeHeaders().getValue("user-agent");
            // Check in the restricted list, and adjust the http11 
            // and keepAlive flags accordingly
            String userAgentValue = userAgentValueMB.toString();
            for (int i = 0; i < restrictedUserAgents.length; i++) {
                if (restrictedUserAgents[i].equals(userAgentValue)) {
                    http11 = false;
                    keepAlive = false;
                }
            }
        }

        // Check for a full URI (including protocol://host:port/)
        ByteChunk uriBC = request.requestURI().getByteChunk();
        if (uriBC.startsWithIgnoreCase("http", 0)) {

            int pos = uriBC.indexOf("://", 0, 3, 4);
            int uriBCStart = uriBC.getStart();
            int slashPos = -1;
            if (pos != -1) {
                byte[] uriB = uriBC.getBytes();
                slashPos = uriBC.indexOf('/', pos + 3);
                if (slashPos == -1) {
                    slashPos = uriBC.getLength();
                    // Set URI as "/"
                    request.requestURI().setBytes
                        (uriB, uriBCStart + pos + 1, 1);
                } else {
                    request.requestURI().setBytes
                        (uriB, uriBCStart + slashPos, 
                         uriBC.getLength() - slashPos);
                }
                MessageBytes hostMB = 
                    request.getMimeHeaders().setValue("host");
                hostMB.setBytes(uriB, uriBCStart + pos + 3, 
                                slashPos - pos - 3);
            }

        }

        // Input filter setup
        InputFilter[] inputFilters = inputBuffer.getFilters();

        // Parse content-length header
        long contentLength = request.getContentLengthLong();
        if (contentLength >= 0) {
            inputBuffer.addActiveFilter
                (inputFilters[Constants.IDENTITY_FILTER]);
            contentDelimitation = true;
        }

        // Parse transfer-encoding header
        MessageBytes transferEncodingValueMB = null;
        if (http11)
            transferEncodingValueMB = 
                request.getMimeHeaders().getValue("transfer-encoding");
        if (transferEncodingValueMB != null) {
            String transferEncodingValue = transferEncodingValueMB.toString();
            // Parse the comma separated list. "identity" codings are ignored
            int startPos = 0;
            int commaPos = transferEncodingValue.indexOf(',');
            String encodingName = null;
            while (commaPos != -1) {
                encodingName = transferEncodingValue.substring
                    (startPos, commaPos).toLowerCase().trim();
                if (!addInputFilter(inputFilters, encodingName)) {
                    // Unsupported transfer encoding
                    error = true;
                    // 501 - Unimplemented
                    response.setStatus(501);
                }
                startPos = commaPos + 1;
                commaPos = transferEncodingValue.indexOf(',', startPos);
            }
            encodingName = transferEncodingValue.substring(startPos)
                .toLowerCase().trim();
            if (!addInputFilter(inputFilters, encodingName)) {
                // Unsupported transfer encoding
                error = true;
                // 501 - Unimplemented
                response.setStatus(501);
            }
        }

        MessageBytes valueMB = request.getMimeHeaders().getValue("host");

        // Check host header
        if (http11 && (valueMB == null)) {
            error = true;
            // 400 - Bad request
            response.setStatus(400);
        }

        parseHost(valueMB);

        if (!contentDelimitation) {
            // If there's no content length and we're using keep-alive 
            // (HTTP/1.0 with keep-alive or HTTP/1.1), assume
            // the client is not broken and didn't send a body
            if (keepAlive) {
                inputBuffer.addActiveFilter
                    (inputFilters[Constants.VOID_FILTER]);
                contentDelimitation = true;
            }
        }

        if (!contentDelimitation)
            keepAlive = false;

    }


    /**
     * Parse host.
     */
    public void parseHost(MessageBytes valueMB) {

        if (valueMB == null || valueMB.isNull()) {
            // HTTP/1.0
            // Default is what the socket tells us. Overriden if a host is 
            // found/parsed
            request.setServerPort(socket.getLocalPort());
            InetAddress localAddress = socket.getLocalAddress();
            // Setting the socket-related fields. The adapter doesn't know 
            // about socket.
            request.setLocalHost(localAddress.getHostName());
            request.serverName().setString(localAddress.getHostName());
            return;
        }

        ByteChunk valueBC = valueMB.getByteChunk();
        byte[] valueB = valueBC.getBytes();
        int valueL = valueBC.getLength();
        int valueS = valueBC.getStart();
        int colonPos = -1;
        if (hostNameC.length < valueL) {
            hostNameC = new char[valueL];
        }

        boolean ipv6 = (valueB[valueS] == '[');
        boolean bracketClosed = false;
        for (int i = 0; i < valueL; i++) {
            char b = (char) valueB[i + valueS];
            hostNameC[i] = b;
            if (b == ']') {
                bracketClosed = true;
            } else if (b == ':') {
                if (!ipv6 || bracketClosed) {
                    colonPos = i;
                    break;
                }
            }
        }

        if (colonPos < 0) {
            if (sslSupport == null) {
                // 80 - Default HTTTP port
                request.setServerPort(80);
            } else {
                // 443 - Default HTTPS port
                request.setServerPort(443);
            }
            request.serverName().setChars(hostNameC, 0, valueL);
        } else {

            request.serverName().setChars(hostNameC, 0, colonPos);

            int port = 0;
            int mult = 1;
            for (int i = valueL - 1; i > colonPos; i--) {
                int charValue = HexUtils.DEC[(int) valueB[i + valueS]];
                if (charValue == -1) {
                    // Invalid character
                    error = true;
                    // 400 - Bad request
                    response.setStatus(400);
                    break;
                }
                port = port + (charValue * mult);
                mult = 10 * mult;
            }
            request.setServerPort(port);

        }

    }

    /*
     * Check for compression
     *
     */
	private boolean isCompressable()
	{
        // Compression only since HTTP 1.1
        if (! http11)
            return false;

        // Check if browser support gzip encoding
        MessageBytes acceptEncodingMB = 
            request.getMimeHeaders().getValue("accept-encoding");
            
        if ((acceptEncodingMB == null) 
            || (acceptEncodingMB.indexOf("gzip") == -1))
            return false;

        // Check if content is not allready gzipped
        MessageBytes contentEncodingMB =
            response.getMimeHeaders().getValue("Content-Encoding");

        if ((contentEncodingMB != null) 
            && (contentEncodingMB.indexOf("gzip") != -1))
            return false;

        // If force mode, allways compress (test purposes only)
        if (compressionLevel == 2)
           return true;

        // Check for incompatible Browser
        if (noCompressionUserAgents != null) {
            MessageBytes userAgentValueMB =  
                request.getMimeHeaders().getValue("user-agent");
            String userAgentValue = userAgentValueMB.toString();

        	if (inStringArray(noCompressionUserAgents, userAgentValue))
        		return false;
        }

        // Check if suffisant len to trig the compression        
        int contentLength = response.getContentLength();
        if ((contentLength == -1) 
            || (contentLength > compressionMinSize)) {
            // Check for compatible MIME-TYPE
            if (compressableMimeTypes != null)
                return (inStringArray(compressableMimeTypes, response.getContentType()));
        }

		return false;
	}
	
    /**
     * When committing the response, we have to validate the set of headers, as
     * well as setup the response filters.
     */
    protected void prepareResponse() {

        boolean entityBody = true;
        contentDelimitation = false;

        OutputFilter[] outputFilters = outputBuffer.getFilters();

        if (http09 == true) {
            // HTTP/0.9
            outputBuffer.addActiveFilter
                (outputFilters[Constants.IDENTITY_FILTER]);
            return;
        }

        int statusCode = response.getStatus();
        if ((statusCode == 204) || (statusCode == 205) 
            || (statusCode == 304)) {
            // No entity body
            outputBuffer.addActiveFilter
                (outputFilters[Constants.VOID_FILTER]);
            entityBody = false;
            contentDelimitation = true;
        }

        MessageBytes methodMB = request.method();
        if (methodMB.equals("HEAD")) {
            // No entity body
            outputBuffer.addActiveFilter
                (outputFilters[Constants.VOID_FILTER]);
            contentDelimitation = true;
        }

        // Check for compression
        boolean useCompression = false;
        if (entityBody && (compressionLevel > 0)) {
            useCompression = isCompressable();
            
            // Change content-length to -1 to force chunking
            if (useCompression) {
                response.setContentLength(-1);
            }
        }

        MimeHeaders headers = response.getMimeHeaders();
        if (!entityBody) {
            response.setContentLength(-1);
        } else {
            String contentType = response.getContentType();
            if (contentType != null) {
                headers.setValue("Content-Type").setString(contentType);
            }
            String contentLanguage = response.getContentLanguage();
            if (contentLanguage != null) {
                headers.setValue("Content-Language")
                    .setString(contentLanguage);
            }
        }

        int contentLength = response.getContentLength();
        if (contentLength != -1) {
            response.getMimeHeaders().setValue("Content-Length")
                .setInt(contentLength);
            outputBuffer.addActiveFilter
                (outputFilters[Constants.IDENTITY_FILTER]);
            contentDelimitation = true;
        } else {
            if (entityBody && http11) {
                outputBuffer.addActiveFilter
                    (outputFilters[Constants.CHUNKED_FILTER]);
                contentDelimitation = true;
                response.addHeader("Transfer-Encoding", "chunked");
            }
        }

        if (useCompression) {
            outputBuffer.addActiveFilter(outputFilters[Constants.GZIP_FILTER]);
            // FIXME: Make content-encoding generation dynamic
            response.setHeader("Content-Encoding", "gzip");
            // Make Proxies happy via Vary (from mod_deflate)
            response.setHeader("Vary", "Accept-Encoding");
        }

        // Add date header
        if (! response.containsHeader("Date"))
          response.addHeader("Date", FastHttpDateFormat.getCurrentDate());

        // Add server header
        /* SJSAS 5022949
        response.addHeader("Server", Constants.SERVER);
         */
        // START SJSAS 5022949
        //response.addHeader("Server", ServerInfo.getServerInfo());
        // END SJSAS 5022949
        // START SJSAS 6415934
        response.addHeader("Server", System.getProperty("product.name"));
        // END SJSAS 6415934

        // Add transfer encoding header
        // FIXME

        if ((entityBody) && (!contentDelimitation)) {
            // Mark as close the connection after the request, and add the 
            // connection: close header
            keepAlive = false;
        }

        // If we know that the request is bad this early, add the
        // Connection: close header.
        keepAlive = keepAlive && !statusDropsConnection(statusCode);
        if (!keepAlive) {
            response.addHeader("Connection", "close");
        } else if (!http11) {
            response.addHeader("Connection", "Keep-Alive");
        }

        // Build the response header
        outputBuffer.sendStatus();

        int size = headers.size();
        for (int i = 0; i < size; i++) {
            outputBuffer.sendHeader(headers.getName(i), headers.getValue(i));
        }
        outputBuffer.endHeaders();

    }


    /**
     * Initialize standard input and output filters.
     */
    protected void initializeFilters() {

        // Create and add the identity filters.
        inputBuffer.addFilter(new IdentityInputFilter());
        outputBuffer.addFilter(new IdentityOutputFilter());

        // Create and add the chunked filters.
        inputBuffer.addFilter(new ChunkedInputFilter());
        outputBuffer.addFilter(new ChunkedOutputFilter());

        // Create and add the void filters.
        inputBuffer.addFilter(new VoidInputFilter());
        outputBuffer.addFilter(new VoidOutputFilter());

        // Create and add buffered input filter
        inputBuffer.addFilter(new BufferedInputFilter());

        // Create and add the chunked filters.
        //inputBuffer.addFilter(new GzipInputFilter());
        outputBuffer.addFilter(new GzipOutputFilter());
    }


    /**
     * Add an input filter to the current request.
     * 
     * @return false if the encoding was not found (which would mean it is 
     * unsupported)
     */
    protected boolean addInputFilter(InputFilter[] inputFilters, 
                                     String encodingName) {
        if (encodingName.equals("identity")) {
            // Skip
        } else if (encodingName.equals("chunked")) {
            inputBuffer.addActiveFilter
                (inputFilters[Constants.CHUNKED_FILTER]);
            contentDelimitation = true;
        } else {
            for (int i = 2; i < inputFilters.length; i++) {
                if (inputFilters[i].getEncodingName()
                    .toString().equals(encodingName)) {
                    inputBuffer.addActiveFilter(inputFilters[i]);
                    return true;
                }
            }
            return false;
        }
        return true;
    }


    /**
     * Specialized utility method: find a sequence of lower case bytes inside
     * a ByteChunk.
     */
    protected int findBytes(ByteChunk bc, byte[] b) {

        byte first = b[0];
        byte[] buff = bc.getBuffer();
        int start = bc.getStart();
        int end = bc.getEnd();

	// Look for first char 
	int srcEnd = b.length;
        
	for (int i = start; i <= (end - srcEnd); i++) {
	    if (Ascii.toLower(buff[i]) != first) continue;
	    // found first char, now look for a match
            int myPos = i+1;
	    for (int srcPos = 1; srcPos < srcEnd; ) {
                if (Ascii.toLower(buff[myPos++]) != b[srcPos++])
		    break;
                if (srcPos == srcEnd) return i - start; // found it
	    }
	}
	return -1;

    }

    /**
     * Determine if we must drop the connection because of the HTTP status
     * code.  Use the same list of codes as Apache/httpd.
     */
    protected boolean statusDropsConnection(int status) {
        return status == 400 /* SC_BAD_REQUEST */ ||
               status == 408 /* SC_REQUEST_TIMEOUT */ ||
               status == 411 /* SC_LENGTH_REQUIRED */ ||
               status == 413 /* SC_REQUEST_ENTITY_TOO_LARGE */ ||
               status == 414 /* SC_REQUEST_URI_TOO_LARGE */ ||
               status == 500 /* SC_INTERNAL_SERVER_ERROR */ ||
               status == 503 /* SC_SERVICE_UNAVAILABLE */ ||
               status == 501 /* SC_NOT_IMPLEMENTED */;
    }


    // START OF SJSAS PE 8.1 6172948
    /**
     * Set the request input buffer size
     */
    public void setBufferSize(int requestBufferSize){
        this.requestBufferSize = requestBufferSize;
    }
    

    /**
     * Return the request input buffer size
     */
    public int getBufferSize(){
        return requestBufferSize;
    }    
    // END OF SJSAS PE 8.1 6172948 
    
}
