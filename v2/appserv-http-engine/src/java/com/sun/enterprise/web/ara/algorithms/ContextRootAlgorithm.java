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

package com.sun.enterprise.web.ara.algorithms;

import com.sun.enterprise.web.connector.grizzly.SelectorThread;
import com.sun.enterprise.web.connector.grizzly.StreamAlgorithm;
import com.sun.enterprise.web.connector.grizzly.Handler;

import java.nio.ByteBuffer;
import java.nio.BufferUnderflowException;
import java.nio.channels.SocketChannel;


/**
 * Parse the request bytes and seek for the context-root value of the 
 * HTTP method.
 *
 * @author Jeanfrancois Arcand
 */
public class ContextRootAlgorithm implements StreamAlgorithm{
 
    private int port = 8080;
    
    private SocketChannel socketChannel;
    
    public ContextRootAlgorithm() {
    }

    
    /**
     * Allocate a <code>ByteBuffer</code>
     * @param useDirect allocate a direct <code>ByteBuffer</code>.
     * @param useView allocate a view <code>ByteBuffer</code>.
     * @return a new <code>ByteBuffer</code>
     */
    public ByteBuffer allocate(boolean useDirect, boolean useView, int size) {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Return the stream content-length. If the content-length wasn't parsed,
     * return -1.
     */
    public int contentLength() {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Return the stream header length. The header length is the length between
     * the start of the stream and the first occurance of character '\r\n' .
     */
    public int headerLength() {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Parse the request line in search of the context-root bytes of the HTTP
     * Method. The <code>ByteBuffer</code> position and limit refer 
     * respectively to the start and the end of the context root.
     * @param byteBuffer The byteBuffer containing the requests bytes
     * @return true if the context-root has been found.
     */
    public boolean parse(ByteBuffer byteBuffer) {
        boolean isFound = false;
                          
        int curPosition = byteBuffer.position();
        int curLimit = byteBuffer.limit();
      
        // Rule a - If nothing, return to the Selector.
        if (byteBuffer.position() == 0)
            return false;
       
        byteBuffer.position(0);
        byteBuffer.limit(curPosition);
        int state =0;
        int start =0;
        int end = 0;        
        
        try {                         
            byte c;            
            
            // Rule b - try to determine the context-root
            while(byteBuffer.hasRemaining()) {
                c = byteBuffer.get();

                // State Machine
                // 0 - Search for the first SPACE ' ' between the method and the
                //     the request URI
                // 1 - Search for the second SPACE ' ' between the request URI
                //     and the method
                switch(state) {
                    case 0: // Search for first ' '
                        if (c == 0x20){
                            state = 1;
                            start = byteBuffer.position() + 1;
                        }
                        break;
                    case 1: // Search for next ' '
                        if (c == 0x20){
                            end = byteBuffer.position() - 1;
                            return true;
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected state");
                }      
            }
            return false;
        } catch (BufferUnderflowException bue) {
            return false;
        } finally {     
            if ( end > 0 ){
                byteBuffer.position(start);
                byteBuffer.limit(end);
            } else {
                byteBuffer.limit(curLimit);
                byteBuffer.position(curPosition);                               
            }
        }       
    }


    /**
     * After parsing the bytes, post process the <code>ByteBuffer</code> 
     * @param byteBuffer the <code>ByteBuffer</code> used by this algorithm
     * @return <code>ByteBuffer</code> used by this algorithm
     */   
    public ByteBuffer postParse(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException();
    }
    
    
    /**
     * Before parsing the bytes, initialize and prepare the algorithm.
     * @param byteBuffer the <code>ByteBuffer</code> used by this algorithm
     * @return <code>ByteBuffer</code> used by this algorithm
     */
    public ByteBuffer preParse(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException();
    }

    
    /**
     * Recycle the algorithm.
     */    
    public void recycle() {
    }

    
    /**
     * Rollback the <code>ByteBuffer</code> to its previous state in case
     * an error as occured.
     */    
    public ByteBuffer rollbackParseState(ByteBuffer byteBuffer) {
        throw new UnsupportedOperationException();
    }
    
        
    /**
     * Return the class responsible for handling OP_READ.
     */
    public Class getReadTask(SelectorThread selectorThread){
        return com.sun.enterprise.web.connector.grizzly.DefaultReadTask.class;
    }   
    // ----------------------------------------------------- Util -----------//
    
    
    /**
     * Dump the ByteBuffer content. This is used only for debugging purpose.
     */
    private String dump(ByteBuffer byteBuffer){                   
        ByteBuffer dd = byteBuffer.duplicate();
        dd.flip();
        
        int length = dd.limit();    
        byte[] dump = new byte[length];
        dd.get(dump,0,length);
        return(new String(dump) + "\n----------------------------" + dd); 
    }


    /**
     * Set the <code>SocketChannel</code> used by this class.
     */
    public void setSocketChannel(SocketChannel socketChannel){
        this.socketChannel = socketChannel;    
    }   
    

    /**
     * Return null as handler aren't required.
     */
    public Handler getHandler(){
        return null;
    }
   
    /**
     * Set the port
     */
    public void setPort(int port){
        this.port = port;
    }
    
    
    /**
     * Return the port
     */
    public int getPort(){
        return port;
    }
}
