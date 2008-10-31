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
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;

/**
 * Abstract superclass for specific types of annotation detectors.
 *
 * @author Jerome Dochez
 */
public class AnnotationDetector {
    
    private final ClassFile classFile;

    public AnnotationDetector(AnnotationScanner scanner) {
        ConstantPoolInfo poolInfo = new ConstantPoolInfo(scanner);
        classFile = new ClassFile(poolInfo);
    }
    
    public boolean hasAnnotationInArchive(ReadableArchive archive) throws IOException {

       Enumeration<String> entries = archive.entries();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement();
            if (entryName.endsWith(".class")) {
                if (containsAnnotation(archive, entryName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsAnnotation(ReadableArchive archive, String entryName) throws IOException {
        boolean result = false;
        // check if it contains top level annotations...
        ReadableByteChannel channel = null;
        try {
            channel = Channels.newChannel(archive.getEntry(entryName));
            if (channel!=null) {
                result = classFile.containsAnnotation(channel, archive.getEntrySize(entryName));
             }
             return result;
        } finally {
            if (channel != null) {
                channel.close();
            }
        }
    }
}
