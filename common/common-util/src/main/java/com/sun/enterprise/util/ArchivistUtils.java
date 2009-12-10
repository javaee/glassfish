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

package com.sun.enterprise.util.shared;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class contains utility methods that handles the archives.
 *
 * @author  Deployment Dev Team
 * @version 
 */
public class ArchivistUtils {

    /** 
     * Utility method that eads the input stream fully and writes the bytes to 
     * the current entry in the output stream. 
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        copyWithoutClose(is, os);
        is.close();
        os.close();
    }     

    /** 
     * Utility method that eads the input stream fully and writes the bytes to 
     * the current entry in the output stream. 
     */
    public static void copyWithoutClose(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[4096];
        int len = 0;
        while (len != -1) {
            try {
                len = is.read(buf, 0, buf.length);
            } catch (EOFException eof){
                break;
            }

            if(len != -1) {
                os.write(buf, 0, len);
            }
        }
        os.flush();
    }
}
