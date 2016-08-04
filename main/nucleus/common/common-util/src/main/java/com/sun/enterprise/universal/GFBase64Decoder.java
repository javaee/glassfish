/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2016 Oracle and/or its affiliates. All rights reserved.
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

public class GFBase64Decoder  { // java.util.Base64 is private constructor and so moving/reusing sun.misc.BASE64Decoder

    private static final char[] pem_array = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'};
    private static final byte[] pem_convert_array = new byte[256];
    byte[] decode_buffer = new byte[4];

    protected int bytesPerAtom() {
        return 4;
    }

    protected int bytesPerLine() {
        return 72;
    }

    protected void decodeAtom(PushbackInputStream var1, OutputStream var2, int var3) throws IOException {
        byte var5 = -1;
        byte var6 = -1;
        byte var7 = -1;
        byte var8 = -1;
        if(var3 < 2) {
            throw new IOException("BASE64Decoder: Not enough bytes for an atom.");
        } else {
            int var4;
            do {
                var4 = var1.read();
                if(var4 == -1) {
                    throw new IOException();
                }
            } while(var4 == 10 || var4 == 13);

            this.decode_buffer[0] = (byte)var4;
            var4 = this.readFully(var1, this.decode_buffer, 1, var3 - 1);
            if(var4 == -1) {
                throw new IOException();
            } else {
                if(var3 > 3 && this.decode_buffer[3] == 61) {
                    var3 = 3;
                }

                if(var3 > 2 && this.decode_buffer[2] == 61) {
                    var3 = 2;
                }

                switch(var3) {
                    case 4:
                        var8 = pem_convert_array[this.decode_buffer[3] & 255];
                    case 3:
                        var7 = pem_convert_array[this.decode_buffer[2] & 255];
                    case 2:
                        var6 = pem_convert_array[this.decode_buffer[1] & 255];
                        var5 = pem_convert_array[this.decode_buffer[0] & 255];
                    default:
                        switch(var3) {
                            case 2:
                                var2.write((byte)(var5 << 2 & 252 | var6 >>> 4 & 3));
                                break;
                            case 3:
                                var2.write((byte)(var5 << 2 & 252 | var6 >>> 4 & 3));
                                var2.write((byte)(var6 << 4 & 240 | var7 >>> 2 & 15));
                                break;
                            case 4:
                                var2.write((byte)(var5 << 2 & 252 | var6 >>> 4 & 3));
                                var2.write((byte)(var6 << 4 & 240 | var7 >>> 2 & 15));
                                var2.write((byte)(var7 << 6 & 192 | var8 & 63));
                        }

                }
            }
        }
    }

    static {
        int var0;
        for(var0 = 0; var0 < 255; ++var0) {
            pem_convert_array[var0] = -1;
        }

        for(var0 = 0; var0 < pem_array.length; ++var0) {
            pem_convert_array[pem_array[var0]] = (byte)var0;
        }

    }





    // from CharacterDecoder

    protected void decodeBufferPrefix(PushbackInputStream var1, OutputStream var2) throws IOException {
    }

    protected void decodeBufferSuffix(PushbackInputStream var1, OutputStream var2) throws IOException {
    }

    protected int decodeLinePrefix(PushbackInputStream var1, OutputStream var2) throws IOException {
        return this.bytesPerLine();
    }

    protected void decodeLineSuffix(PushbackInputStream var1, OutputStream var2) throws IOException {
    }

    protected int readFully(InputStream var1, byte[] var2, int var3, int var4) throws IOException {
        for(int var5 = 0; var5 < var4; ++var5) {
            int var6 = var1.read();
            if(var6 == -1) {
                return var5 == 0?-1:var5;
            }

            var2[var5 + var3] = (byte)var6;
        }

        return var4;
    }

    public void decodeBuffer(InputStream var1, OutputStream var2) throws IOException {
        int var4 = 0;
        PushbackInputStream var5 = new PushbackInputStream(var1);
        this.decodeBufferPrefix(var5, var2);

        while(true) {
            try {
                int var6 = this.decodeLinePrefix(var5, var2);

                int var3;
                for(var3 = 0; var3 + this.bytesPerAtom() < var6; var3 += this.bytesPerAtom()) {
                    this.decodeAtom(var5, var2, this.bytesPerAtom());
                    var4 += this.bytesPerAtom();
                }

                if(var3 + this.bytesPerAtom() == var6) {
                    this.decodeAtom(var5, var2, this.bytesPerAtom());
                    var4 += this.bytesPerAtom();
                } else {
                    this.decodeAtom(var5, var2, var6 - var3);
                    var4 += var6 - var3;
                }

                this.decodeLineSuffix(var5, var2);
            } catch (IOException var8) {
                this.decodeBufferSuffix(var5, var2);
                return;
            }
        }
    }

    public byte[] decodeBuffer(String var1) throws IOException {
        byte[] var2 = new byte[var1.length()];
        var1.getBytes(0, var1.length(), var2, 0);
        ByteArrayInputStream var3 = new ByteArrayInputStream(var2);
        ByteArrayOutputStream var4 = new ByteArrayOutputStream();
        this.decodeBuffer(var3, var4);
        return var4.toByteArray();
    }

    public byte[] decodeBuffer(InputStream var1) throws IOException {
        ByteArrayOutputStream var2 = new ByteArrayOutputStream();
        this.decodeBuffer(var1, var2);
        return var2.toByteArray();
    }

    public ByteBuffer decodeBufferToByteBuffer(String var1) throws IOException {
        return ByteBuffer.wrap(this.decodeBuffer(var1));
    }

    public ByteBuffer decodeBufferToByteBuffer(InputStream var1) throws IOException {
        return ByteBuffer.wrap(this.decodeBuffer(var1));
    }

}
