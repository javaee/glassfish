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

package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.annotation.introspection.AnnotationScanner;
import com.sun.enterprise.deployment.annotation.introspection.ClassFile;
import com.sun.enterprise.deployment.annotation.introspection.ConstantPoolInfo;
import org.glassfish.api.deployment.archive.ReadableArchive;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileFilter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * Subclass for connector annotation detection.
 * Connector annotation detector need to scan for top level jars as well
 *
 */
public class ConnectorAnnotationDetector extends AnnotationDetector {

    public ConnectorAnnotationDetector(AnnotationScanner scanner) {
        super(scanner);
    }
    
    @Override
    public boolean hasAnnotationInArchive(ReadableArchive archive) throws IOException {

        Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement();
            if (entryName.endsWith(".class")) {
                if (containsAnnotation(archive, entryName)) {
                    return true;
                }
            } 

            // scan classes in top level jars
            File archiveFile = new File(archive.getURI());
            File[] jarFiles = archiveFile.listFiles(new FileFilter() {
                 public boolean accept(File pathname) {
                     return (pathname.isFile() &&
                            pathname.getAbsolutePath().endsWith(".jar"));
                 }
            });

            if (jarFiles != null && jarFiles.length > 0) {
                for (File file : jarFiles) {
                    JarFile jarFile = null; 
                    try {
                        jarFile = new JarFile(file);
                        Enumeration<JarEntry> jarEntries = jarFile.entries();
                        while (jarEntries.hasMoreElements()) {
                            JarEntry jarEntry = jarEntries.nextElement();
                            if (jarEntry.getName().endsWith(".class")) {
                                if (containsAnnotation(jarFile.getInputStream(
                                    jarEntry), jarEntry.getSize())) {
                                    return true;
                                }
                            }
                        } 
                    } finally {
                        if (jarFile != null) {
                            jarFile.close();
                        }
                    }
                }
            }
        }
        return false;
    }
}
