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
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * The abstract class provides a set of methods to out outputs into the sepcified archive file.
 */
public abstract class AbstractStaticWeaveOutputHandler{
    protected JarOutputStream outputStreamHolder=null;
    
    /**
     * create directory into target directory, or insert directory entry into outputstream.
     * @param dirPath
     * @throws IOException
     */
    abstract public void addDirEntry(String dirPath)throws IOException;
    
    /**
     * Write entry bytes into target, this is usually called if class has been tranformed
     * @param targetEntry
     * @param entryBytes
     * @throws IOException
     */
    abstract public void addEntry(JarEntry targetEntry,byte[] entryBytes)throws IOException;
    
    /**
     * Write entry into target, this method usually copy original class into target.
     * @param jis
     * @param entry
     * @throws IOException
     */
    abstract public void addEntry(InputStream jis,JarEntry entry) throws IOException,URISyntaxException;

    
    /**
     * Close the output stream.
     * @throws IOException
     */
    public void closeOutputStream() throws IOException {
        if(outputStreamHolder!=null){
            outputStreamHolder.close();
        }
    }
    
    /**
     * Get the ouput stream instance.
     * @return
     */
    public JarOutputStream getOutputStream(){
        return this.outputStreamHolder;
    }


    // This is part of the ugly workaround for a design flaw
    // in the JDK zip API, the entry will not write into the target zip file 
    // properly if this method not being gone through.
    protected void readwriteStreams(InputStream in, OutputStream out) throws IOException
    {
        int numRead;
        byte[] buffer = new byte[8*1024];

        while ((numRead = in.read(buffer,0,buffer.length)) != -1) {
            out.write(buffer,0,numRead);
        }   
    }
}
