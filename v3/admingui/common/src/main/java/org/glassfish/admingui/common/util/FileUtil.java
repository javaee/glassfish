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
package org.glassfish.admingui.util;

import java.io.*;
import java.io.IOException;
import java.util.jar.JarFile;

/**
 * File pathname/location management utility methods
 */
public class FileUtil {

	/**
	 * Deletes all files, and subdirectories under dir
	 * Returns true if the specified file/dir is deleted
	 * If the delete fails, it's likely due to improper permissions, or
     * didn't exist.
	 */

	public static boolean delete(File f) {
		boolean deleteStatus = false;
		if(f != null) {
			if(f.isDirectory()) {
				deleteDirContents(f);
				deleteStatus = f.delete();
			}
			else {
				deleteStatus = f.delete();
			}
		}
		return deleteStatus;
	}

	public static boolean delete(String file) {
		return delete(new File(file));
	}

	private static boolean deleteDirContents(File dir) {
		boolean deleteStatus = false;
		try {
			File dirCon = dir.getCanonicalFile();//may throw IOException
			String[] files = dirCon.list();
			for(int i = 0; i < files.length; i++) {
				File f = new File(dir, files[i]);
				File filePath = f.getCanonicalFile();
				if(filePath.getParentFile().equals(dirCon)) {
					deleteStatus = delete(filePath);
				}
				else {
					//may be a symlink, not the same parent.
					//try deleting in anyway
					deleteStatus = delete(filePath);
				}
			} 
		} catch(IOException ex) { 
			deleteStatus = false;
			throw new SecurityException(ex);
		  }
		return deleteStatus;
	}
    /**
        Gets the name of the temporary folder where the admin-server would be
        creating some temporary data. Note that this method is not a pure
        accessor in the sense that it will create the folder on disk if
        it does not exist.
        @return String representing the absolute path of temporary folder, which        may be null if the file creation fails.
    */
	public static String getTempDirPath() throws Exception {
		String localTmpDir = System.getProperty("java.io.tmpDir");
		File tempFolder = new File(localTmpDir, GUI_TEMPDIR_NAME);

		if(!tempFolder.exists()) {
			boolean couldCreate = tempFolder.mkdirs();

			//Log/Throw Exception if the dir can't be created.
		
		}
		return tempFolder.getCanonicalPath();

	}

        public static boolean isJarFile(File file) {
            //Check whether this is a valid file
            //for now return true.
            try {
                boolean result = false;
                
                if(file.getName().endsWith(".jar")) {
                    JarFile jar = new JarFile(file);
                    if ( jar != null ) {
                        result = jar.entries().hasMoreElements();
                    }
                    jar.close();
                    return result ;
                } else {
                    return false;
                }
                
            } catch (IOException ex) {
                return false;
            }
        }
        
        public static String getFileName(String filePath) { 
            String name = new File(filePath).getName();
            int index = name.indexOf(".");
            if (index > 0)
                return name.substring(0, index);
            else
                return filePath;
            
        }
        
	public static void main(String[] args) {
		FileUtil.delete("/export/home/TEMP/jws/glassfish/admin-gui/admin-jsf/src/docroot/");
	}
    	private static final String GUI_TEMPDIR_NAME="admingui";
}
