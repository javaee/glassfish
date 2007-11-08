

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

import java.io.IOException;

import org.apache.tomcat.util.buf.ByteChunk;

import org.apache.coyote.InputBuffer;
import org.apache.coyote.Request;

/**
 * Input filter interface.
 * 
 * @author Remy Maucherat
 */
public interface InputFilter extends InputBuffer {


    /**
     * Read bytes.
     * 
     * @return Number of bytes read.
     */
    public int doRead(ByteChunk chunk, Request unused)
        throws IOException;


    /**
     * Some filters need additional parameters from the request. All the 
     * necessary reading can occur in that method, as this method is called
     * after the request header processing is complete.
     */
    public void setRequest(Request request);


    /**
     * Make the filter ready to process the next request.
     */
    public void recycle();


    /**
     * Get the name of the encoding handled by this filter.
     */
    public ByteChunk getEncodingName();


    /**
     * Set the next buffer in the filter pipeline.
     */
    public void setBuffer(InputBuffer buffer);


    /**
     * End the current request.
     * 
     * @return 0 is the expected return value. A positive value indicates that
     * too many bytes were read. This method is allowed to use buffer.doRead
     * to consume extra bytes. The result of this method can't be negative (if
     * an error happens, an IOException should be thrown instead).
     */
    public long end()
        throws IOException;


}
