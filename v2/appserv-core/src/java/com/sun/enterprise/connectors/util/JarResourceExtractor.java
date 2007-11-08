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

package com.sun.enterprise.connectors.util;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.*;

import com.sun.logging.LogDomains;


/**
 * JarResourceExtractor: JarResourceExtractor maps all resources included in a Zip or Jar file.
 * Additionaly, it provides a method to extract one as a blob.
 * 
 * @author Sivakumar Thyagarajan
 */

public final class JarResourceExtractor {
    static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    //resourceName as String Vs contents as byte[]
    private Hashtable htJarContents = new Hashtable();
    
    /**
     * creates a JarResourceExtractor. It extracts all resources from a Jar into an
     * internal hashtable, keyed by resource names.
     * 
     * @param jarFileName
     *            a jar or zip file
     */
    public JarResourceExtractor(String jarFileName) {
        init(jarFileName);
    }
    
    /**
     * Extracts a jar resource as a blob.
     * 
     * @param name
     *            a resource name.
     */
    public byte[] getResource(String name) {
        _logger.finer("getResource: " + name);
        return (byte[]) htJarContents.get(name);
    }
    
    /** initializes internal hash tables with Jar file resources. */
    private void init(String jarFileName) {
        try {
            //extract resources and put them into the hashtable.
            FileInputStream fis = new FileInputStream(jarFileName);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);            
            extractResources(zis);
        } catch (Exception ex){
            ex.printStackTrace();
        }
        
    }
    
    /**
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void extractResources(ZipInputStream zis) throws FileNotFoundException, IOException {
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            _logger.finer("ExtractResources : " + ze.getName());
            extractZipEntryContents(ze, zis);
        }
    }
    
    /**
     * @param zis
     * @throws IOException
     */
    private void extractZipEntryContents(ZipEntry ze, ZipInputStream zis) throws IOException {
            if (ze.isDirectory()) {
                return;
            }
            
            _logger.finer("ze.getName()=" + ze.getName() + ","
                        + "getSize()=" + ze.getSize());

            byte[] b = getZipEntryContents(ze,zis);
            //If it is a jar go RECURSIVE !!
            if(ze.getName().trim().endsWith(".jar")){
                _logger.finer("JAR - going into it !!");
                BufferedInputStream bis = new BufferedInputStream( (new ByteArrayInputStream(b)));
                extractResources(new ZipInputStream(bis));
            } else {
                //add to internal resource hashtable
                htJarContents.put(ze.getName(), b );
                if (ze.getName().trim().endsWith("class")){ 
                    _logger.finer("CLASS added " + ze.getName());
                }
                _logger.finer(ze.getName() + ",size="
                        + b.length + ",csize=" + ze.getCompressedSize());
            }
    }
    
    private byte[] getZipEntryContents(ZipEntry ze, ZipInputStream zis) throws IOException{
        int size = (int) ze.getSize();
        
        byte[] b = null;
        // -1 means unknown size.
        if (size != -1) {
            //got a proper size, read 'size' bytes
            b = new byte[(int) size];
            
            int rb = 0;
            int chunk = 0;
            
            while (((int) size - rb) > 0) {
                chunk = zis.read(b, rb, (int) size - rb);
                if (chunk == -1) {
                    break;
                }
                rb += chunk;
            }
        } else {
            //size of entry unknown .. keep on reading till we hit a -1
            ArrayList al = new ArrayList();
            int c = 0;
            while( (c = zis.read()) != -1) {
                al.add(new Byte((byte)c));
            }
            Byte[] btArr = (Byte[])al.toArray(new Byte[]{});
            b = new byte[btArr.length];
            _logger.finer("ByteArray length" + btArr.length);
            for (int i = 0; i < btArr.length; i++) {
                b[i] = btArr[i].byteValue();
            }
        }
        
        return b;
    }
}
