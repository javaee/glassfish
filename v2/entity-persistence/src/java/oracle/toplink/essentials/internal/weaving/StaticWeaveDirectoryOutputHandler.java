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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.jar.JarEntry;

/**
 * The class provides a set of methods to pack passed-in entries into the sepcified archive file.
 * the class handle directory output.
 */
public class StaticWeaveDirectoryOutputHandler extends AbstractStaticWeaveOutputHandler{
    private URL source=null;
    private URL target=null;
    
    
    /**
     * Construct an instance of StaticWeaveDirectoryOutputHandler.
     * @param source
     * @param target
     */
    public StaticWeaveDirectoryOutputHandler(URL source,URL target){
        this.source=source;
        this.target=target;
    }
    /**
     * create directory into target directory.
     * @param dirPath
     * @throws IOException
     */
    public void addDirEntry(String dirPath)throws IOException {
       File file = new File(this.target.getPath()+File.separator+dirPath).getAbsoluteFile();
       if (!file.exists()){
           file.mkdirs();
       }
    }
    
    /**
     * Write entry bytes into target, this method is usually invoked  if class has been tranformed
     * @param targetEntry
     * @param entryBytes
     * @throws IOException
     */
    public void addEntry(JarEntry targetEntry,byte[] entryBytes)throws IOException{
        File target  = new File(this.target.getPath()+targetEntry.getName()).getAbsoluteFile();
        if(!target.exists()) {
            target.createNewFile();
        }
        (new FileOutputStream(target)).write(entryBytes);
    }
    
    /**
     * Write entry into target, this method usually copy original class into target.
     * @param jis
     * @param entry
     * @throws IOException
     */
    public void addEntry(InputStream jis,JarEntry entry) throws IOException,URISyntaxException {    
        File target  = new File(this.target.getPath()+entry.getName()).getAbsoluteFile();
        if(!target.exists()) {
            target.createNewFile();
        }
        if((new File(this.source.toURI())).isDirectory()){
            File sourceEntry = new File(this.source.getPath()+entry.getName());
            FileInputStream fis = new FileInputStream(sourceEntry);
            byte[] classBytes = new byte[fis.available()];
            fis.read(classBytes);
            (new FileOutputStream(target)).write(classBytes);
        }else{
            readwriteStreams(jis,(new FileOutputStream(target)));
        }
    }
}
