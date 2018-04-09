/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.ssi;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;


/**
 * Class that extends ServletOuputStream, used as a wrapper from within
 * <code>SsiInclude</code>
 *
 * @author Bip Thelin
 * @version $Revision: 1.3 $, $Date: 2007/02/13 19:16:20 $
 * @see ServletOutputStream and ByteArrayOutputStream
 */
public class ByteArrayServletOutputStream extends ServletOutputStream {
    /**
     * Our buffer to hold the stream.
     */
    protected ByteArrayOutputStream buf = null;

    /**
     * Construct a new ServletOutputStream.
     */
    public ByteArrayServletOutputStream() {
        buf = new ByteArrayOutputStream();
    }


    /**
     * @return the byte array.
     */
    public byte[] toByteArray() {
        return buf.toByteArray();
    }


    /**
     * Write to our buffer.
     *
     * @param b The parameter to write
     */
    @Override
    public void write(int b) {
        buf.write(b);
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new IllegalStateException();
    }
}
