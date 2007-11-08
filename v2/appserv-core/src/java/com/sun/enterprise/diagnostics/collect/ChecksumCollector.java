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
package com.sun.enterprise.diagnostics.collect;



import com.sun.logging.LogDomains;
import com.sun.enterprise.diagnostics.DiagnosticException;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.Defaults;
import com.sun.enterprise.diagnostics.util.DiagnosticServiceHelper;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CheckedInputStream;
import java.util.zip.CRC32;

/**
 * Collects check sum information for binaries
 * @author Manisha Umbarje
 */
public class ChecksumCollector implements Collector {
    
    private String source;
    private String destFolder;
    private String destFile;
    private BufferedWriter writer;
    private WritableData dataObj;
    private static final String BIN_FOLDER = File.separator + "bin";
    private static final String LIB_FOLDER = File.separator + "lib";
    private static final String JAR_EXT = ".jar";
    private static final String DLL_EXT = "*.dll";
    private static final String SO_EXT = "*.so";
    private static Logger logger = 
    LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    /** 
     * Creates a new instance of ChecksumCollector 
     * @param destFolder destination folder in which checksum information
     * is copied.
     */
    public ChecksumCollector(String source, String destFolder) {
        this.destFolder = destFolder;
        this.source = source;
        dataObj = new WritableDataImpl(source, DataType.CHECKSUM);
    }
    
    /**
     * Captures check sum information
     * @throw DiagnosticException 
     */
    public Data capture() throws DiagnosticException {
        dataObj.addRow(Arrays.asList(new String[]{"Name", "Length", "Checksum"}));
        captureChecksum(source + LIB_FOLDER, getFilter());
        captureChecksum(source + BIN_FOLDER, null);
        return dataObj;
    }
    
    /**
     * Captures checksum of files specified by the filter
     * @param sourceName source folder from where files are copied
     * @param filter file name filter
     * @throw DiagnosticException
     */
    private void captureChecksum(String sourceName, FilenameFilter filter)
        throws DiagnosticException {
        
        File fileObj = new File(sourceName);
        String[] fileNames = fileObj.list(filter);
        if(fileNames != null) {
            int length = fileNames.length;
            for (int i = 0 ; i < length; i++) {
                dataObj.addRow(generateCRC32Checksum(sourceName, fileNames[i]));
            }
        }

    }
    
    /**
     * Generates check sum
     * @param file name of the file for which checkum is calculated
     * @return list containing  file name, length and checksum 
     */
    private List<String> generateCRC32Checksum(String parent, String fileName) 
    throws DiagnosticException {
        try {
        // First item being name of file, second - length; third being checksum
        String file = parent + File.separator + fileName;
        List<String> checksumInfo = new ArrayList(3);
        CRC32 crc32 = new CRC32();
        int length = 0;
        BufferedInputStream fileinputstream = new BufferedInputStream(
                new FileInputStream(new File(file)));
        for( CheckedInputStream checkedinputstream = new CheckedInputStream(
        fileinputstream, crc32); checkedinputstream.read() != -1;)
                length++;
        long cksum = crc32.getValue();
        fileinputstream.close();
        fileinputstream = null;
        crc32 = null;
        checksumInfo.add(fileName);
        checksumInfo.add(Integer.toString(length));
        checksumInfo.add(Long.toString(cksum));
        return checksumInfo;
        } catch(Exception e) {
           //ignore
            logger.log(Level.FINE, "diagnostic-service.compute_checksum_failed"
                    ,new Object[]{fileName, e.getMessage()});
            return null;
        }
    }
    
    /**
     * Gets filter which recognizes .jar and .so/.dll files
     * @return FilenameFilter 
     */
    private FilenameFilter getFilter() {
        final String[] exts ;
        if(isSolaris())
            exts = new String[] {JAR_EXT, SO_EXT};
        else
            exts = new String[] {JAR_EXT, DLL_EXT};
        return new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return name.endsWith(exts[0]) ||
                        name.endsWith(exts[1]);
            }
        };
    }
    
    private boolean isSolaris() {
        return DiagnosticServiceHelper.isSolaris();
    }
    
    private void writeToFile(List<String> checksumInfo) throws 
            DiagnosticException{
        if (checksumInfo != null) {
            try {
                writer.write("\n");
                for(String entry : checksumInfo) {
                    writer.write(entry + "\t");
                } //for
             }catch(IOException ioe) {
               //ignore
            }
            
       }//if
    }//writeToFile
}
