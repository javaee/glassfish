/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006-2017 Oracle and/or its affiliates. All rights reserved.
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

/*
 * KEDAR/MURALI has made some changes to this class
 * so that it works with installer(LogDomains esp.).
 */
package admin.util;

import java.io.*;


import java.nio.channels.*;
import java.nio.ByteBuffer;

public class FileUtils {
    /**
     * Copies a file.
     *
     * @param from Name of file to copy
     * @param to   Name of new file
     * @throws IOException if an error while copying the content
     */
    private static void copy(String from, String to) throws IOException {
        //if(!StringUtils.ok(from) || !StringUtils.ok(to))
        if (from == null || to == null)
            throw new IllegalArgumentException("null or empty filename argument");

        File fin = new File(from);
        File fout = new File(to);

        copy(fin, fout);
    }

    public static void copy(File fin, File fout) throws IOException {

        InputStream inStream = new BufferedInputStream(new FileInputStream(fin));
        FileOutputStream outStream = new FileOutputStream(fout);
        copy(inStream, outStream, fin.length());
    }

    private static void copy(InputStream in, FileOutputStream out, long size) throws IOException {

        try {
            copyWithoutClose(in, out, size);
        }
        finally {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
        }
    }

    private static void copyWithoutClose(InputStream in, FileOutputStream out, long size) throws IOException {

        ReadableByteChannel inChannel = Channels.newChannel(in);
        FileChannel outChannel = out.getChannel();
        outChannel.transferFrom(inChannel, 0, size);
    }

    private static void copy(InputStream in, OutputStream os, long size) throws IOException {
        if (os instanceof FileOutputStream) {
            copy(in, (FileOutputStream) os, size);
        }
        else {
            ReadableByteChannel inChannel = Channels.newChannel(in);
            WritableByteChannel outChannel = Channels.newChannel(os);
            if (size == 0) {

                ByteBuffer byteBuffer = ByteBuffer.allocate(10240);
                int read;
                do {
                    read = inChannel.read(byteBuffer);
                    if (read > 0) {
                        byteBuffer.limit(byteBuffer.position());
                        byteBuffer.rewind();
                        outChannel.write(byteBuffer);
                        byteBuffer.clear();
                    }
                }
                while (read != -1);
            }
            else {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Long.valueOf(size).intValue());
                inChannel.read(byteBuffer);
                byteBuffer.rewind();
                outChannel.write(byteBuffer);
            }
        }
    }
    public static void main(String... args) {
        try {
            copy(args[0], args[1]);
        }
        catch (IOException ex) {
            System.out.println("ERROR: " + ex);
        }
    }
}
