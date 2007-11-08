/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.enterprise.web.connector.grizzly.ssl;

import com.sun.enterprise.web.connector.grizzly.DefaultProcessorTask;
import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import org.apache.coyote.ActionCode;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.coyote.http11.Constants;
import org.apache.coyote.http11.InputFilter;
import org.apache.coyote.http11.InternalInputBuffer;
import org.apache.coyote.http11.filters.BufferedInputFilter;
import org.apache.tomcat.util.net.SSLSupport;

/**
 * Simple <code>ProcessorTask</code> that configure the <code>outputBuffer</code>
 * using an instance of <code>SSLOutputBuffer</code>. All the request/response
 * operations are delegated to the <code>ProcessorTask</code>
 *
 * @author Jeanfrancois Arcand
 */
public class SSLProcessorTask extends DefaultProcessorTask{
    
    private SSLReadTask sslReadTask;

    // ----------------------------------------------------- Constructor ---- //

    public SSLProcessorTask(){
        this(true,true);
    }
        
    public SSLProcessorTask(boolean init, boolean bufferResponse){    
        super(init,bufferResponse);
    }

    
    /**
     * Initialize the stream and the buffer used to parse the request.
     */
    public void initialize(){
        started = true;   
        request = new Request();

        response = new Response();
        response.setHook(this);
        
        inputBuffer = new InternalInputBuffer(request,requestBufferSize); 
        outputBuffer = new SSLOutputBuffer(response,maxHttpHeaderSize,
                                           bufferResponse);
        request.setInputBuffer(inputBuffer);
       
        response.setOutputBuffer(outputBuffer);
        request.setResponse(response);

        initializeFilters();
    }
    
   
    /**
     * Send an action to the connector.
     * 
     * @param actionCode Type of the action
     * @param param Action parameter
     */
    public void action(ActionCode actionCode, Object param) {
 
        if (actionCode == ActionCode.ACTION_REQ_SSL_ATTRIBUTE ) {
            try {
                if (sslSupport != null) {
                    Object sslO = sslSupport.getCipherSuite();
                    if (sslO != null)
                        request.setAttribute
                            (SSLSupport.CIPHER_SUITE_KEY, sslO);
                    sslO = sslReadTask.doPeerCertificateChain(false);
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
                SelectorThread.logger().log(Level.WARNING,
                        "processorTask.errorSSL" ,e);
            }
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
                    Object sslO = sslReadTask.doPeerCertificateChain(true);
                    if( sslO != null) {
                        request.setAttribute
                            (SSLSupport.CERTIFICATE_KEY, sslO);
                    }
                } catch (Exception e) {
                    SelectorThread.logger().log(Level.WARNING,
                            "processorTask.exceptionSSLcert",e);
                }
            }
        } else {
            super.action(actionCode,param);
        }
    } 

    
    /**
     * Set the <code>SSLReadTask</code> associated with this instance. The
     * <code>SSLReadTask</code> is needed when handling peer certificate chain.
     */
    public void setSslReadTask(SSLReadTask sslReadTask) {
        this.sslReadTask = sslReadTask;
    }
}
