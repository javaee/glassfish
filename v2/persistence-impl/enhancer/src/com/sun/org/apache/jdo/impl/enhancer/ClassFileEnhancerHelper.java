/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * Copyright 2005 The Apache Software Foundation.
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

package com.sun.org.apache.jdo.impl.enhancer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 *  This is a helper-class to perform some useful operations outside a
 *  byte code enhancer and delegate the real work to the enhancer.
 */
public class ClassFileEnhancerHelper
{
    /**
     *  Enhances a classfile.
     *
     *  @param  enhancer  The enhancer to delegate the work to.
     *  @param  in        The input stream with the Java class.
     *  @param  out       The output stream to write the enhanced class to.
     *
     *  @return  Has the input stream been enhanced?
     *
     *  @exception  EnhancerUserException  If something went wrong.
     *  @exception  EnhancerFatalError     If something went wrong.
     *
     *  @see  ClassFileEnhancer#enhanceClassFile
     */
    static public boolean enhanceClassFile(ClassFileEnhancer enhancer,
                                           InputStream      in,
                                           OutputStream     out)
        throws EnhancerUserException,
        EnhancerFatalError
    {
        return enhancer.enhanceClassFile(in, new OutputStreamWrapper(out));
    }

    /**
     *  Enhances a zip file. The zip file is given as a uip input stream.
     *  It's entries are read and - if necessary - individually enhanced.
     *  The output stream has the same compressíon (if any) as the input
     *  stream.
     *
     *  @param  enhancer  The enhancer.
     *  @param  zip_in    The zip input stream.
     *  @param  zip_out   The zip output stream.
     *
     *  @return  <code>true</code> if at least one entry of the zip file has
     *           been enhanced, <code>false</code> otherwise.
     *
     *  @exception  EnhancerUserException  If something went wrong.
     *  @exception  EnhancerFatalError     If something went wrong.
     *
     *  @see  ClassFileEnhancer#enhanceClassFile
     */
    static public boolean enhanceZipFile(ClassFileEnhancer enhancer,
                                         ZipInputStream zip_in,
                                         ZipOutputStream zip_out)
        throws EnhancerUserException,
        EnhancerFatalError
    {
        boolean enhanced = false;
        try {
            CRC32 crc32 = new CRC32();
            ZipEntry entry;
            while ((entry = zip_in.getNextEntry()) != null) {
                InputStream in = zip_in;
                final ZipEntry out_entry = new ZipEntry(entry);

                // try to enhance
                if (isClassFileEntry(entry)) {
                    // enhance the classfile
                    // we have to copy the classfile, because if it won't be
                    // enhanced, the OutputStream is empty and we have to
                    // re-read the InputStream, which is impossible with a
                    // ZipInputStream (no mark/reset)
                    in = openZipEntry(zip_in);
                    in.mark(Integer.MAX_VALUE);
                    final ByteArrayOutputStream tmp
                        = new ByteArrayOutputStream();
                    if (enhancer.enhanceClassFile(in, tmp)) {
                        enhanced = true;
                        final byte[] bytes = tmp.toByteArray();
                        tmp.close();
                        in.close();
                        modifyZipEntry(out_entry, bytes, crc32);
                        in = new ByteArrayInputStream(bytes);
                    } else {
                        // the classfile has not been enhanced
                        in.reset();
                    }
                }

                // copy the entry
                zip_out.putNextEntry(out_entry);
                copyZipEntry(in, zip_out);
                zip_out.closeEntry();

                if (in != zip_in) {
                    in.close();
                }
            }
        } catch (IOException ex) {
            throw new EnhancerFatalError(ex);
        }

        return enhanced;
    }

    /**
     *  Copies a zip entry from one stream to another.
     *
     *  @param  in   The inout stream.
     *  @param  out  The output stream.
     *
     *  @exception  IOException  If the stream access failed.
     */
    static private void copyZipEntry(InputStream  in,
                                     OutputStream out)
        throws IOException
    {
        int b;
        while ((in.available() > 0) && (b = in.read()) > -1) {
            out.write(b);
        }
    }

    /**
     *  Opens the next zip entry of a zip input stream and copies it to
     *  a <code>java.io.ByteArrayOutputStream</code>. It's byte array is made
     *  available via an <code>java.io.ByteArrayInputStream</code> which is
     *  returned.
     *
     *  @param  in  The zip input stream.
     *
     *  @return  The newly created input stream with the next zip entry.
     *
     *  @exception  IOException  If an I/O operation failed.
     */
    static private InputStream openZipEntry(ZipInputStream in)
        throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copyZipEntry(in, out);

        return new ByteArrayInputStream(out.toByteArray());
    }

    /**
     *  Modifies the given zip entry so that it can be added to zip file.
     *  The given zip entry represents an enhanced class, so the zip entry
     *  has to get the correct size and checksum (but only if the entry won't
     *  be compressed).
     *
     *  @param  entry  The zip entry to modify.
     *  @param  bytes  The uncompressed byte representation of the classfile.
     *  @param  crc32  The checksum evaluator.
     */
    private static void modifyZipEntry(ZipEntry entry,
                                       byte []  bytes,
                                       CRC32    crc32)
    {
        entry.setSize(bytes.length);
        if (entry.getMethod() == 0) {
            //no compression (ZipInputStream.STORED - not accessible)
            crc32.reset();
            crc32.update(bytes);
            entry.setCrc(crc32.getValue());
            entry.setCompressedSize(bytes.length);
        }
    }

    /**
     *  Determines if a given entry represents a classfile.
     *
     *  @return  Does the given entry represent a classfile?
     */
    private static boolean isClassFileEntry(ZipEntry entry)
    {
        return entry.getName().endsWith(".class");
    }
}
