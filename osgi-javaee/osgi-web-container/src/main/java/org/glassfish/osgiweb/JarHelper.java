/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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


package org.glassfish.osgiweb;

import java.io.*;
import java.net.URLConnection;
import java.net.URI;
import java.util.jar.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.ByteBuffer;

/**
 * A utility class to help reading/writing content of JarFile from/to stream.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class JarHelper
{
    private static final Logger logger = Logger.getLogger(
            JarHelper.class.getPackage().getName());

    public static interface Visitor {
        void visit(JarEntry je);
    }

    public static void accept(JarInputStream jis, Visitor visitor) throws IOException {
        JarEntry je;
        while ((je = jis.getNextJarEntry()) != null)
        {
            logger.logp(Level.FINE, "JarHelper", "accept", "je = ${0}", new Object[]{je});
            visitor.visit(je);
        }
    }
    
    /**
     * A utility method which reads contents from a URLConnection and
     * writes it out a Jar output stream. It reads everything except manifest
     * from the input. Closing of output stream is caller's responsibility.
     *
     * @param con URLConnection to be used as input
     * @param os  Output stream to write to
     * @param m   Manifest to be written out - can't be null
     * @throws IOException
     */
    public static void write(URLConnection con, OutputStream os, Manifest m)
    {
        try
        {
            InputStream in = con.getInputStream();
            JarInputStream jis = null;
            JarOutputStream jos = null;
            try
            {
                // We can assume the underlying stream is a JarInputStream.
                jis = new JarInputStream(in);
                jos = new JarOutputStream(os, m);
                write(jis, jos);
            }
            finally
            {
                try
                {
                    if (jos != null)
                    {
                        jos.close();
                    }
                }
                catch (IOException ioe)
                {
                }
                try
                {
                    if (jis != null)
                    {
                        jis.close();
                    }
                }
                catch (IOException ioe)
                {
                }
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                }
            }
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * A utility method to help write content of a Jar input stream
     * to a Jar output stream. It reads everything except manifest from
     * the supplied InputStream. Closing of streams is caller's responsibility.
     *
     * @param jis input stream to read from
     * @param jos output stream to write to
     * @throws IOException
     */
    public static void write(JarInputStream jis, JarOutputStream jos) throws IOException
    {
        // Copy each entry from input to output
        // The manifest.mf is automatically excluded,
        // as JarInputStream.getNextEntry never returns that.
        ByteBuffer byteBuffer = ByteBuffer.allocate(10240);
        ZipEntry ze;
        while ((ze = jis.getNextEntry()) != null)
        {
            logger.logp(Level.FINE, "JarHelper", "write", "ze = {0}", new Object[]{ze});
            jos.putNextEntry(ze);
            copy(jis, jos, byteBuffer);
            jos.closeEntry();
        }
    }

    /**
     * A utility method to make a JarInputStream out of the contents of a directory.
     * It uses a Pipe and a separate thread to write the contents to avoid deadlock.
     * It accepts a Runnable to take action once the spwaned thread has finished writing.
     * It can be used to delete the directory.
     *
     * @param dir Directory which contains the exploded bits
     * @param action A runnable to be called after output has been written
     * @return a InputStream
     */
    public static InputStream makeJar(final File dir, final Runnable action) throws IOException {
        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos);
        new Thread() {
            @Override
            public void run() {
                try {
                    Manifest m;
                    File mf = new File(dir, JarFile.MANIFEST_NAME);
                    if (mf.exists()) {
                        FileInputStream mfis = new FileInputStream(mf);
                        try {
                            m = new Manifest(mfis);
                        } finally {
                            mfis.close();
                        }
                    } else {
                        m = new Manifest();
                        m.getMainAttributes().putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
                    }
                    final JarOutputStream jos = new JarOutputStream(pos, m);
                    final ByteBuffer buf = ByteBuffer.allocate(10240);
                    final URI baseURI = dir.toURI();
                    // System.out.println("baseURI = " + baseURI);
                    dir.listFiles(new FileFilter() {
                        public boolean accept(File f) {
                            try {
                                URI entryURI = f.toURI();
//                                System.out.println("entryURI = " + entryURI);
                                String entryPath = baseURI.relativize(entryURI).getPath();
//                                System.out.println("entryPath = " + entryPath);
                                if (entryPath.equals(JarFile.MANIFEST_NAME)) return false;
                                jos.putNextEntry(new JarEntry(entryPath));
                                if (f.isDirectory()) {
                                    f.listFiles(this); // recursion
                                } else {
                                    FileInputStream in = new FileInputStream(f);
                                    try {
                                        copy(in, jos, buf);
                                    } finally {
                                        try {
                                            in.close();
                                        } catch (IOException e) {
                                            // ignore
                                        }
                                    }
                                }
                                jos.closeEntry();
                            } catch (IOException e) {
                                logger.logp(Level.WARNING, "JarHelper", "makeJar", "Exception occurred", e);
                                throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                            }
                            return false;
                        }
                    });
                    jos.close();
                    pos.close();
                    if (action != null) {
                        action.run();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e); // TODO(Sahoo): Proper Exception Handling
                }
            }
        }.start();
        return pis;
    }

    /**
     * Copies input to output. To avoid unnecessary allocation of byte buffers,
     * this method takes a byte buffer as argument. It clears the byte buffer
     * at the end of the operation.
     * @param in
     * @param out
     * @param byteBuffer
     */
    public static void copy(InputStream in, OutputStream out, ByteBuffer byteBuffer)
            throws IOException{
        try {
            ReadableByteChannel inChannel = Channels.newChannel(in);
            WritableByteChannel outChannel = Channels.newChannel(out);

            int read;
            do {
                read = inChannel.read(byteBuffer);
                if (read>0) {
                    byteBuffer.limit(byteBuffer.position());
                    byteBuffer.rewind();
                    outChannel.write(byteBuffer);
                    logger.logp(Level.FINE, "JarHelper", "write",
                            "Copied {0} bytes", new Object[]{read});
                    byteBuffer.clear();
                }
            } while (read != -1);
        } finally {
            byteBuffer.clear();
        }
    }
}
