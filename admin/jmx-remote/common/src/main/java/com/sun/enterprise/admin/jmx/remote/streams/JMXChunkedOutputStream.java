/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.admin.jmx.remote.streams;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class JMXChunkedOutputStream extends OutputStream {
    private OutputStream out = null;
    private byte[] buffer = null;
    private int bufCount = 0;

    public JMXChunkedOutputStream(OutputStream out) {
        this.out = out;
        buffer = new byte[8192];
    }

    public void close() throws IOException {
        if (bufCount > 0)
            flush();
        out.close();
    }

    public void flush() throws IOException {
        if (bufCount > 0)
            flushBuffer();
        else
            out.flush();
    }

    private void flushBuffer() throws IOException {
        writeObject(buffer, 0, bufCount);
        bufCount = 0;
    }

    public void writeEOF(int padLen) throws IOException {
        DataOutputStream dO = new DataOutputStream(out);
        dO.writeInt(0);
        // Kludge:: For some wierd reason, the StreamingOutputStream of
        //          HttpURLConnection is not counting the requestmessage object's
        //          length as the number of bytes written.
        //          Hence, we will send some padding bytes at the end to fool
        //          StreamingOutputStream.
        dO.write(new byte[padLen],0,padLen);
        dO.flush();
    }

    public void write(byte[] b) throws IOException {
        if (b == null)
            throw (new NullPointerException("byte array is null"));
        write(b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        if (b == null)
            throw (new NullPointerException("byte array is null"));
        if (off < 0 || len < 0 || (off+len) > b.length)
            throw (new IndexOutOfBoundsException(
                                    "offset="+off+
                                    ", len="+len+
                                    ", (off+len)="+(off+len)+
                                    ", b.length="+b.length+
                                    ", (off+len)>b.length="+
                                        ((off+len)>b.length)));
        if (len == 0)
            return;
        if (bufCount > 0 && (bufCount+len) >= 8192) {
            flushBuffer();
        }
        if (len >= 8192) {
            writeObject(b, off, len);
            return;
        }
        writeBuffer(b, off, len);
    }

    public void write(int by) throws IOException {
        byte b = (byte) by;
        if (bufCount > 0 && (bufCount+1) >= 8192) {
            flushBuffer();
        }
        buffer[bufCount] = b;
        bufCount++;
    }

    private void writeBuffer(byte[] b, int off, int len) {
        System.arraycopy(b, off, buffer, bufCount, len);
        bufCount += len;
    }

    private void writeObject(byte[] b, int off, int len) 
            throws IOException {
        DataOutputStream dO = new DataOutputStream(out);
        dO.writeInt(len);
        dO.write(b, off, len);
        dO.flush();
    }
}

