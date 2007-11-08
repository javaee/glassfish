package com.sun.enterprise.tools.admingui.util;
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
import java.util.jar.JarInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.io.IOException;
import java.io.OutputStream;

public class JarExtract {
    
    public static void extract(String fileName, String dir) throws IOException {
        if(fileName == null || dir == null ) {
            //localize this message
            throw new IOException("extract method: dir, or fileName is null");
        }
        File f = new File(fileName);
        if(!f.exists()) {
            //localize this message.
            throw new IOException(fileName + ": file not found");
        }
        FileInputStream fin = new FileInputStream(f);
        JarInputStream jin = new JarInputStream(fin);
         ZipEntry e;
        
         while((e =jin.getNextEntry()) != null ) {    
            extract(jin, e, dir);
        }
        
    }
    
    private static void extract(JarInputStream jin, ZipEntry e, String dir) 
        throws IOException {
        
        File f = new File(dir + File.separatorChar + e.getName().replace('/', File.separatorChar));
	if (e.isDirectory()) {
	    if (!f.exists() && !f.mkdirs() || !f.isDirectory()) {
                //localize this mesg.
		throw new IOException(f + ": could not create directory");
	    }
	} else {
	    if (f.getParent() != null) {
		File d = new File(f.getParent());
		if (!d.exists() && !d.mkdirs() || !d.isDirectory()) {
                    //localize this mesg.
		    throw new IOException(d + ": could not create directory");
		}
	    }
	    OutputStream os = new FileOutputStream(f);
	    byte[] b = new byte[512];
	    int len;
	    while ((len = jin.read(b, 0, b.length)) != -1) {
		os.write(b, 0, len);
	    }
	    jin.closeEntry();
	    os.close();
	}
    
    }
    
}
