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
package com.sun.enterprise.web.connector.grizzly;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
/**
 * This interface defines the methods an instance of a <code>ReadTask</code> 
 * will invoke. The <code>ReadTask</code> will use an implementation of this 
 * interface tp predict if the NIO code>ByteBuffer</code> has been fully 
 * read and can be processed.
 *
 * <code>ReadTask</code> will invoke the method in the following order:
 *
 * (a) allocate(...)
 * (b) preParse(...)
 * (c) parse(...)
 * (d) contentLength() AND headerLength();
 * (d) postParse(...)
 * 
 * The algorithm will stop once (c) return <code>true</code>
 * 
 * @author Jean-Francois Arcand
 */
public interface StreamAlgorithm{
    
    
    /**
     * Return the stream content-length. If the content-length wasn't parsed,
     * return -1.
     */
    public int contentLength();
    
    
    /**
     * Return the stream header length. The header length is the length between
     * the start of the stream and the first occurance of character '\r\n' .
     */
    public int headerLength();
    
    
    /**
     * Allocate a <code>ByteBuffer</code>
     * @param useDirect allocate a direct <code>ByteBuffer</code>.
     * @param useView allocate a view <code>ByteBuffer</code>.
     * @param size the size of the newly created <code>ByteBuffer</code>.
     * @return a new <code>ByteBuffer</code>
     */
    public ByteBuffer allocate(boolean useDirect, boolean useView, int size);
    
    
    /**
     * Before parsing the bytes, initialize and prepare the algorithm.
     * @param byteBuffer the <code>ByteBuffer</code> used by this algorithm
     * @return <code>ByteBuffer</code> used by this algorithm
     */
    public ByteBuffer preParse(ByteBuffer byteBuffer);
    
    
    /**
     * Parse the <code>ByteBuffer</code> and try to determine if the bytes
     * stream has been fully read from the <code>SocketChannel</code>.
     * @paran byteBuffer the bytes read.
     * @return true if the algorithm determines the end of the stream.
     */
    public boolean parse(ByteBuffer byteBuffer);
    
    
    /**
     * After parsing the bytes, post process the <code>ByteBuffer</code> 
     * @param byteBuffer the <code>ByteBuffer</code> used by this algorithm
     * @return <code>ByteBuffer</code> used by this algorithm
     */
    public ByteBuffer postParse(ByteBuffer byteBuffer);  
    
    
    /**
     * Recycle the algorithm.
     */
    public void recycle();
    
    
    /**
     * Rollback the <code>ByteBuffer</code> to its previous state in case
     * an error as occured.
     */
    public ByteBuffer rollbackParseState(ByteBuffer byteBuffer);  
    
    
    /**
     * The <code>Handler</code> associated with this algorithm.
     */
    public Handler getHandler();

    
    /**
     * Set the <code>SocketChannel</code> used by this algorithm
     */
    public void setSocketChannel(SocketChannel socketChannel);
    
    
    /**
     * Set the <code>port</code> this algorithm is used.
     */
    public void setPort(int port);
    
    
    /**
     * Return the port
     */
    public int getPort();
    
    
    /**
     * Return the class responsible for handling OP_READ.
     */
    public Class getReadTask(SelectorThread selectorThread);
}

