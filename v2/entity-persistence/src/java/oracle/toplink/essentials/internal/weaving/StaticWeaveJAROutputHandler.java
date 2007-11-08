/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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

package oracle.toplink.essentials.internal.weaving;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;

/**
 * The class provides a set of methods to pack passing in entries into the sepcified archive file.
 * the class JAR output.
 */
public class StaticWeaveJAROutputHandler extends AbstractStaticWeaveOutputHandler{

    /**
     * Construct an instance of StaticWeaveJAROutputHandler
     * @param outputStreamHolder
     */
    public StaticWeaveJAROutputHandler(JarOutputStream outputStreamHolder){
        super.outputStreamHolder=outputStreamHolder;
    }
    
    /**
     * Add directory entry into outputstream.
     * @param dirPath
     * @throws IOException
     */
    public void addDirEntry(String dirPath)throws IOException {
        try{
            JarEntry newEntry = new JarEntry(dirPath);
            newEntry.setSize(0);
            addEntry(newEntry, null);
        }catch(ZipException e){
            //ignore duplicate directory entry exceptions.
        }
    }
    
    /**
     * Write entry bytes into target, this method is usually called if class has been tranformed
     * @param targetEntry
     * @param entryBytes
     * @throws IOException
     */
    public void addEntry(JarEntry targetEntry,byte[] entryBytes)throws IOException{
        outputStreamHolder.putNextEntry(targetEntry);
        if(entryBytes!=null){
            outputStreamHolder.write(entryBytes);
        }
        outputStreamHolder.closeEntry();
    }
    
    /**
     * Write entry into target, this method usually copy original class into target.
     * @param jis
     * @param entry
     * @throws IOException
     */
    public void addEntry(InputStream jis,JarEntry entry) throws IOException,URISyntaxException {    
        outputStreamHolder.putNextEntry(entry);
        if(!entry.isDirectory()){
           readwriteStreams(jis,outputStreamHolder);
        }
        outputStreamHolder.closeEntry();
    }
}
