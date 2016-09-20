/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.universal;

import java.io.*;
import java.nio.ByteBuffer;

public class GFBase64Encoder { // java.util.Base64 is private constructor and so moving/reusing sun.misc.BASE64Encoder

    private static final char[] pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};

    protected PrintStream pStream;

    protected void encodeBufferPrefix(OutputStream var1) throws IOException {
        this.pStream = new PrintStream(var1);
    }

    protected void encodeBufferSuffix(OutputStream var1) throws IOException {
    }

    protected void encodeLinePrefix(OutputStream var1, int var2) throws IOException {
    }

    protected void encodeLineSuffix(OutputStream var1) throws IOException {
        this.pStream.println();
    }

    protected int readFully(InputStream var1, byte[] var2) throws IOException {
        for(int var3 = 0; var3 < var2.length; ++var3) {
            int var4 = var1.read();
            if(var4 == -1) {
                return var3;
            }

            var2[var3] = (byte)var4;
        }

        return var2.length;
    }

    public void encode(InputStream var1, OutputStream var2) throws IOException {
        byte[] var5 = new byte[this.bytesPerLine()];
        this.encodeBufferPrefix(var2);

        while(true) {
            int var4 = this.readFully(var1, var5);
            if(var4 == 0) {
                break;
            }

            this.encodeLinePrefix(var2, var4);

            for(int var3 = 0; var3 < var4; var3 += this.bytesPerAtom()) {
                if(var3 + this.bytesPerAtom() <= var4) {
                    this.encodeAtom(var2, var5, var3, this.bytesPerAtom());
                } else {
                    this.encodeAtom(var2, var5, var3, var4 - var3);
                }
            }

            if(var4 < this.bytesPerLine()) {
                break;
            }

            this.encodeLineSuffix(var2);
        }

        this.encodeBufferSuffix(var2);
    }

    public void encode(byte[] var1, OutputStream var2) throws IOException {
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);
        this.encode((InputStream)var3, var2);
    }

    public String encode(byte[] var1) {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);
        String var4 = null;

        try {
            this.encode((InputStream)var3, var2);
            var4 = var2.toString("8859_1");
            return var4;
        } catch (Exception var6) {
            throw new Error("CharacterEncoder.encode internal error");
        }
    }

    private byte[] getBytes(ByteBuffer var1) {
        byte[] var2 = null;
        if(var1.hasArray()) {
            byte[] var3 = var1.array();
            if(var3.length == var1.capacity() && var3.length == var1.remaining()) {
                var2 = var3;
                var1.position(var1.limit());
            }
        }

        if(var2 == null) {
            var2 = new byte[var1.remaining()];
            var1.get(var2);
        }

        return var2;
    }

    public void encode(ByteBuffer var1, OutputStream var2) throws IOException {
        byte[] var3 = this.getBytes(var1);
        this.encode(var3, var2);
    }

    public String encode(ByteBuffer var1) {
        byte[] var2 = this.getBytes(var1);
        return this.encode(var2);
    }

    public void encodeBuffer(InputStream var1, OutputStream var2) throws IOException {
        byte[] var5 = new byte[this.bytesPerLine()];
        this.encodeBufferPrefix(var2);

        int var4;
        do {
            var4 = this.readFully(var1, var5);
            if(var4 == 0) {
                break;
            }

            this.encodeLinePrefix(var2, var4);

            for(int var3 = 0; var3 < var4; var3 += this.bytesPerAtom()) {
                if(var3 + this.bytesPerAtom() <= var4) {
                    this.encodeAtom(var2, var5, var3, this.bytesPerAtom());
                } else {
                    this.encodeAtom(var2, var5, var3, var4 - var3);
                }
            }

            this.encodeLineSuffix(var2);
        } while(var4 >= this.bytesPerLine());

        this.encodeBufferSuffix(var2);
    }

    public void encodeBuffer(byte[] var1, OutputStream var2) throws IOException {
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);
        this.encodeBuffer((InputStream)var3, var2);
    }

    public String encodeBuffer(byte[] var1) {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1);

        try {
            this.encodeBuffer((InputStream)var3, var2);
        } catch (Exception var5) {
            throw new Error("CharacterEncoder.encodeBuffer internal error");
        }

        return var2.toString();
    }

    public void encodeBuffer(ByteBuffer var1, OutputStream var2) throws IOException {
        byte[] var3 = this.getBytes(var1);
        this.encodeBuffer(var3, var2);
    }

    public String encodeBuffer(ByteBuffer var1) {
        byte[] var2 = this.getBytes(var1);
        return this.encodeBuffer(var2);
    }

    protected int bytesPerAtom() {
        return 3;
    }

    protected int bytesPerLine() {
        return 57;
    }

    protected void encodeAtom(OutputStream var1, byte[] var2, int var3, int var4) throws IOException {
        byte var5;
        if(var4 == 1) {
            var5 = var2[var3];
            byte var6 = 0;
            boolean var7 = false;
            var1.write(pem_array[var5 >>> 2 & 63]);
            var1.write(pem_array[(var5 << 4 & 48) + (var6 >>> 4 & 15)]);
            var1.write(61);
            var1.write(61);
        } else {
            byte var8;
            if(var4 == 2) {
                var5 = var2[var3];
                var8 = var2[var3 + 1];
                byte var9 = 0;
                var1.write(pem_array[var5 >>> 2 & 63]);
                var1.write(pem_array[(var5 << 4 & 48) + (var8 >>> 4 & 15)]);
                var1.write(pem_array[(var8 << 2 & 60) + (var9 >>> 6 & 3)]);
                var1.write(61);
            } else {
                var5 = var2[var3];
                var8 = var2[var3 + 1];
                byte var10 = var2[var3 + 2];
                var1.write(pem_array[var5 >>> 2 & 63]);
                var1.write(pem_array[(var5 << 4 & 48) + (var8 >>> 4 & 15)]);
                var1.write(pem_array[(var8 << 2 & 60) + (var10 >>> 6 & 3)]);
                var1.write(pem_array[var10 & 63]);
            }
        }

    }
}
