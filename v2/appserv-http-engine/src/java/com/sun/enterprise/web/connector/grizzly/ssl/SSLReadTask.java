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

import com.sun.enterprise.web.connector.grizzly.DefaultReadTask;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.WorkerThread;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import org.apache.tomcat.util.net.SSLImplementation;
import org.apache.tomcat.util.net.SSLSupport;
/**
 * SSL support over NIO. This <code>Task</code> handles the SSL requests
 * using a non blocking socket. The SSL handshake is done using this class. 
 * Once the handshake is successful, the <code>SSLProcessorTask</code> is 
 * executed. 
 *
 * @author Jean-Francois Arcand
 */
public class SSLReadTask extends DefaultReadTask {
    
    /**
     * The <code>SSLEngine</code> required to encrypt/decrypt SSL request bytes.
     */
    protected SSLEngine sslEngine = null;

    /**
     * Decrypted ByteBuffer default size.
     */
    protected int appBBSize = 5 * 4096;
    
    
    /**
     * Encrypted ByteBuffer default size.
     */
    protected int inputBBSize = 5 * 4096;

    
    /**
     * The encrypted input ByteBuffer.
     */
    protected ByteBuffer inputBB;
    
    
    /**
     * The encrupted output ByteBuffer
     */
    protected ByteBuffer outputBB;

    
    /**
     * Is the handshake completed.
     */
    protected boolean handshake = true;


    /**
     * The Coyote SSLImplementation used to retrive the <code>SSLContext</code>
     */
    protected SSLImplementation sslImplementation;

    
    // -------------------------------------------------------------------- //
    
    public SSLReadTask() {
        ;//
    }
    
    
    /**
     * Initialize this object.
     */
    public void initialize(StreamAlgorithm algorithm,
                      boolean useDirectByteBuffer, boolean useByteBufferView){
        type = READ_TASK;    
        this.algorithm = algorithm;       
        inputStream = new SSLByteBufferInputStream();
        
        this.useDirectByteBuffer = useDirectByteBuffer;
        this.useByteBufferView = useByteBufferView; 
    }

    
    /**
     * Allocate themandatory <code>ByteBuffer</code>s. Since the ByteBuffer
     * are maintaned on the <code>SSLWorkerThread</code> lazily, this method
     * makes sure the ByteBuffers are properly allocated and configured.
     */
    public void allocateBuffers(){
        final SSLWorkerThread workerThread = 
                (SSLWorkerThread)Thread.currentThread();
        
        int expectedSize = sslEngine.getSession().getPacketBufferSize();
        if (inputBBSize < expectedSize){
            inputBBSize = expectedSize;
        }

        if (inputBB != null && inputBB.capacity() < inputBBSize) {
            ByteBuffer newBB = ByteBuffer.allocate(inputBBSize);
            inputBB.flip();
            newBB.put(inputBB);
            inputBB = newBB;                                
        } else if (inputBB == null && workerThread.getInputBB() != null ){
            inputBB = workerThread.getInputBB();
        } else if (inputBB == null){
            inputBB = ByteBuffer.allocate(inputBBSize);
        }      
        
        if (workerThread.getOutputBB() == null) {
            outputBB = ByteBuffer.allocate(inputBBSize);
        } else {
            outputBB = workerThread.getOutputBB();
        }
        
        if (byteBuffer == null && workerThread.getByteBuffer() == null){
            byteBuffer = ByteBuffer.allocate(inputBBSize * 2);
        } else if (byteBuffer == null){
            byteBuffer = workerThread.getByteBuffer();
        }

        expectedSize = sslEngine.getSession().getApplicationBufferSize();
        if ( expectedSize > byteBuffer.capacity() ) {
            ByteBuffer newBB = ByteBuffer.allocate(expectedSize);
            byteBuffer.flip();
            newBB.put(byteBuffer);
            byteBuffer = newBB;
        }   
         
        // Make sure the same ByteBuffer is used.
        workerThread.setInputBB(inputBB);
        workerThread.setOutputBB(outputBB);  
        workerThread.setByteBuffer(byteBuffer);
   
        outputBB.position(0);
        outputBB.limit(0); 
        workerThread.setSSLEngine(sslEngine);
    }
    
    
    /**
     * Register the <code>SelectionKey</code> with the <code>Selector</code>.
     * The <code>SSLEngine</code> is attached because it is impossible to 
     * keep-alive an ssl connection without re-using the same SSLEngine.
     */
    public void registerKey(){
        key.attach(sslEngine);
        super.registerKey();
    }
    
    
    /**
     * Perform an SSL handshake using an SSLEngine. If the handshake is 
     * successfull, process the connection.
     */
    public void doTask() throws IOException {        
        int count = 0;
        Exception exception = null;
        boolean keepAlive = false;
        SSLEngineResult result;  
        final SSLWorkerThread workerThread = 
                (SSLWorkerThread)Thread.currentThread();
        
        SocketChannel socketChannel = (SocketChannel)key.channel();
        try {
            allocateBuffers();          
            if (!doHandshake(SSLUtils.getReadTimeout())) {
                keepAlive = false;
                count = -1;
            } else {   
                if (!handshake){
                    count = doRead(inputBB);
                    if (count == -1){
                        keepAlive = false;
                        return;
                    }
                } else {
                    handshake = false;
                }
                               
                try{
                    inputStream.setByteBuffer(byteBuffer);
                    keepAlive = process();                            
                } catch (IOException ex) {
                    keepAlive = false; 
                }
            }     
        } catch (IOException ex) {         
            exception = ex;
            keepAlive = false;
        } catch (Throwable ex) {
            Logger logger = SSLSelectorThread.logger();
            if ( logger.isLoggable(Level.FINE) ){
                logger.log(Level.FINE,"doRead",ex);
            }            
            count = -1;
            keepAlive = false;
        } finally { 
            manageKeepAlive(keepAlive,count,exception);
        }            
    } 


    private int doRead(ByteBuffer inputBB){ 
        int count = -1;
        try{
            // Read first bytes to avoid continuing if the client
            // closed the connection.
            count = ((SocketChannel)key.channel()).read(inputBB);
            if (count != -1){
                // Decrypt the bytes we just read.
                byteBuffer =
                        SSLUtils.unwrapAll(byteBuffer,inputBB,sslEngine);
                final SSLWorkerThread workerThread =
                        (SSLWorkerThread)Thread.currentThread();
                workerThread.setByteBuffer(byteBuffer);
                workerThread.setInputBB(inputBB);
            }
            return count;
        } catch(IOException ex){
            return -1;
        } finally {
            if (count == -1){
                try{
                    sslEngine.closeInbound();
                } catch (SSLException ex){
                    ;
                }
            } 
        }
    }
    
    
    /**
     * Execute a non blocking SSL handshake.
     */    
    protected boolean doHandshake(int timeout) throws IOException{
        HandshakeStatus handshakeStatus = HandshakeStatus.NEED_UNWRAP;
        boolean OK = true;    
        final SSLWorkerThread workerThread = 
                (SSLWorkerThread)Thread.currentThread();
        try{ 
            if ( handshake ) {
                byteBuffer = SSLUtils.doHandshake
                             (key,byteBuffer,inputBB,outputBB,sslEngine,
                              handshakeStatus,timeout);

                if (doRead(inputBB) == -1){
                    throw new EOFException();
                }
            }  
        } catch (EOFException ex) {
            Logger logger = SSLSelectorThread.logger();
            if ( logger.isLoggable(Level.FINE) ){
                logger.log(Level.FINE,"doHandshake",ex);
            }            
            OK = false;
        } finally {
            workerThread.setOutputBB(outputBB);
        }
        return OK;
    }    

    
    /**
     * Get the peer certificate list by enatiating a new handshake.
     * @return Object[] An array of X509Certificate.
     */
    protected Object[] doPeerCertificateChain(boolean needClientAuth) 
            throws IOException {
        Logger logger = SSLSelectorThread.logger();
        final SSLWorkerThread workerThread = 
                (SSLWorkerThread)Thread.currentThread();
     
        Certificate[] certs=null;
        try {
            certs = sslEngine.getSession().getPeerCertificates();
        } catch( Throwable t ) {
            if ( logger.isLoggable(Level.FINE))
                logger.log(Level.FINE,"Error getting client certs",t);
        }
 
        if (certs == null && needClientAuth){
            sslEngine.getSession().invalidate();
            sslEngine.setNeedClientAuth(true);
            sslEngine.beginHandshake();         
                      
            ByteBuffer origBB = workerThread.getByteBuffer();
            outputBB = workerThread.getOutputBB();

            // In case the application hasn't read all the body bytes.
            if ( origBB.position() != origBB.limit() ){
                byteBuffer = ByteBuffer.allocate(origBB.capacity());
            } else {
                byteBuffer = origBB;
            }
            byteBuffer.clear();
            outputBB.position(0);
            outputBB.limit(0); 
            
            handshake= true;
            try{
                doHandshake(0);
            } catch (Throwable ex){
                if ( logger.isLoggable(Level.FINE))
                    logger.log(Level.FINE,"Error during handshake",ex);   
                return null;
            } finally {
                byteBuffer = origBB;
                handshake= false;
                workerThread.setByteBuffer(byteBuffer);   
                inputStream.setByteBuffer(byteBuffer);
                byteBuffer.clear();
            }            

            try {
                certs = sslEngine.getSession().getPeerCertificates();
            } catch( Throwable t ) {
                if ( logger.isLoggable(Level.FINE))
                    logger.log(Level.FINE,"Error getting client certs",t);
            }
        }
        
        if( certs==null ) return null;
        
        X509Certificate[] x509Certs = new X509Certificate[certs.length];
        for(int i=0; i < certs.length; i++) {
            if( certs[i] instanceof X509Certificate ) {
                x509Certs[i] = (X509Certificate)certs[i];
            } else {
                try {
                    byte [] buffer = certs[i].getEncoded();
                    CertificateFactory cf =
                    CertificateFactory.getInstance("X.509");
                    ByteArrayInputStream stream = new ByteArrayInputStream(buffer);
                    x509Certs[i] = (X509Certificate)
                    cf.generateCertificate(stream);
                } catch(Exception ex) { 
                    logger.log(Level.INFO,"Error translating cert " + certs[i],
                                     ex);
                    return null;
                }
            }
            
            if(logger.isLoggable(Level.FINE))
                logger.log(Level.FINE,"Cert #" + i + " = " + x509Certs[i]);
        }
        
        if(x509Certs.length < 1)
            return null;
            
        return x509Certs;
    }
    
    
    /**
     * Configure the <code>SSLProcessorTask</code>.
     */
    protected void configureProcessorTask(){
        super.configureProcessorTask();
        SSLSupport sslSupport = sslImplementation.getSSLSupport(sslEngine);
        ((SSLProcessorTask)processorTask).setSSLSupport(sslSupport);
        ((SSLProcessorTask)processorTask).setSslReadTask(this);
    }
    
    
    /**
     * Return the <code>ProcessorTask</code> to the pool.
     */
    public void detachProcessor(){
        if ( processorTask != null ){
            ((SSLProcessorTask)processorTask).setSSLSupport(null);
            ((SSLProcessorTask)processorTask).setSslReadTask(null);
        }
        super.detachProcessor();
    }   
    
    /**
     * Process the request using the decrypted <code>ByteBuffer</code>. The
     * <code>SSLProcessorTask</code>
     */               
    protected boolean process() throws IOException{
        boolean keepAlive = false;     
        SocketChannel socketChannel = (SocketChannel)key.channel();
        Socket socket = socketChannel.socket();
        algorithm.setSocketChannel(socketChannel);    
        inputStream.setSelectionKey(key);
                
        if (processorTask == null){
            attachProcessor(selectorThread.getProcessorTask());
        }
        
        // Always true with the NoParsingAlgorithm
        if ( algorithm.parse(byteBuffer) ){ 
           return executeProcessorTask();
        } else {
           // Never happens with the default StreamAlgorithm
           return true;
        }
    }   

    
    /**
     * Recycle this object so it can be re-used. Make sure all ByteBuffers
     * are properly recycled.
     */
    public void recycle(){
        if (byteBuffer != null){ 
            try{
                WorkerThread workerThread = (WorkerThread)Thread.currentThread();    
                workerThread.setByteBuffer(byteBuffer);
            } catch (ClassCastException ex){
                // Avoid failling if the Grizzly extension doesn't support
                // the WorkerThread interface.               
                Logger logger = SSLSelectorThread.logger();
                if (logger.isLoggable(Level.FINEST))
                    logger.log(Level.FINEST,"recycle",ex);                
            } finally {
                byteBuffer = algorithm.postParse(byteBuffer);   
                byteBuffer.clear();
            }
        }    
        handshake = true;
        
        inputStream.recycle();
        algorithm.recycle();
        key = null;
        inputStream.setSelectionKey(null);       

        if ( inputBB != null ) {
            inputBB.clear();
        }
        
        if ( outputBB != null ){
            outputBB.clear();
            outputBB.position(0);
            outputBB.limit(0);
        }
             
        inputBB = null;
        outputBB = null;
        byteBuffer = null;                
        sslEngine = null;
    }
    
    
    /**
     * Set the Coyote <code>SSLImplemenation</code>
     */
    public void setSSLImplementation(SSLImplementation sslImplementation){
        this.sslImplementation = sslImplementation;
    }
    
    
    /**
     * Set true if the handshake already occured.
     */
    public void setHandshake(boolean handshake){
        this.handshake = handshake;
    }
    
    
    /**
     * Return the handshake status.
     */
    public boolean getHandshake(){
        return handshake;
    }
     
    
    /**
     * Set the <code>SSLEngine</code>.
     */
    public void setSSLEngine(SSLEngine sslEngine){
        this.sslEngine = sslEngine;     
    }
    
    
    /**
     * Return the <code>SSLEngine</code> used by this instance.
     */
    public SSLEngine getSSLEngine(){
        return sslEngine;
    }

    
    /**
     * Return the encrypted <code>ByteBuffer</code> used to handle request.
     */
    public ByteBuffer getInputBB(){
        return inputBB;
    }
    
    
    /**
     * Set the encrypted <code>ByteBuffer</code> used to handle request.
     */    
    public void setInputBB(ByteBuffer inputBB){
        this.inputBB = inputBB;
    }

    public ByteBuffer getOutputBB() {
        return outputBB;
    }

    public void setOutputBB(ByteBuffer outputBB) {
        this.outputBB = outputBB;
    }
}
