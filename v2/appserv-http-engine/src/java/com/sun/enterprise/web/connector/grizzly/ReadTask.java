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

/**
 * Main <code>Task</code> to handle OP_READ.
 *
 * @author Jeanfrancois Arcand
 */
public interface ReadTask extends Task{
    /**
     * Force this task to always use the same <code>ProcessorTask</code> instance.
     */
    public void attachProcessor(ProcessorTask processorTask);

    
    /**
     * Return the <code>ProcessorTask</code> to the pool.
     */
    public void detachProcessor();

    
    /**
     * Return the underlying <code>ByteBuffer</code> used by this class.
     */
    public ByteBuffer getByteBuffer();

    
    public void initialize(StreamAlgorithm algorithm, 
            boolean useDirectByteBuffer, boolean useByteBufferView);

    
    /**
     * If the attached byteBuffer was already filled, tell the
     * Algorithm to re-use the bytes.
     */
    public void setBytesAvailable(boolean bytesAvailable);

    
    /**
     * Complete the processing.
     */
    public void terminate(boolean keepAlive);
    
    
    /**
     * Set the time in milliseconds this <code>Task</code> is allowed to be idle.
     */
    public void setIdleTime(long time);
    
    
    /**
     * Return the time in milliseconds this <code>Task</code> is allowed to be idle.
     */
    public long getIdleTime();
}
