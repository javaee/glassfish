
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

package com.sun.enterprise.admin.mbeans.custom.loading;

import com.sun.enterprise.admin.mbeans.custom.CMBStrings;
import com.sun.enterprise.admin.server.core.CustomMBeanException;
import com.sun.enterprise.admin.servermgmt.RepositoryConfig;
import com.sun.enterprise.admin.servermgmt.pe.PEFileLayout;
import com.sun.enterprise.util.SystemPropertyConstants;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.BufferedInputStream;
import com.sun.enterprise.util.OS;

/** A custom class loader to load the MBeans defined by users. This class-loader delegates
 * to system-class-loader, <code> sun.misc.Launcher$AppClassLoader@169e11 for Sun's JRE</code>.
 * One instance of this class-loader is created at the startup and it loads the classes from
 * a specific location that is passed to it. If the bits of the class that has been loaded
 * by this instance of the class are now changed on the disk, a new instance of the class loader
 * is first created and that instance of this class-loader loads the new bits giving birth to a new class.
 * More about this in the design document.
 * @since SJSAS 9.0
 */
public final class MBeanClassLoader extends ClassLoader {
    
    private URL url;
    private ClassLoader parent;
    /** Creates a new instance of MBeanClassLoader */
    /*
    public MBeanClassLoader(final URL urlToLoadFrom, final ClassLoader parent) throws IllegalArgumentException {
        super(parent);
        if (urlToLoadFrom == null || parent == null)
            throw new IllegalArgumentException("null arguments"); //TODO
        initialize(urlToLoadFrom, parent);
    }
    public MBeanClassLoader(final File diskFolder, final ClassLoader parent) throws IllegalArgumentException {
        super(parent);
        if (diskFolder == null)
            throw new IllegalArgumentException("null arguments"); //TODO
        if (!diskFolder.isDirectory())
            throw new IllegalArgumentException("invalid, can load only from a folder"); //TODO
        if (!diskFolder.exists())
            throw new IllegalArgumentException("invalid, does not exist: " + diskFolder.getPath()); //TODO
        if (!diskFolder.canRead())
            throw new IllegalArgumentException("invalid, can not read: " + diskFolder.getPath()); //TODO
        try {
            initialize(diskFolder.toURL());
        } catch (final MalformedURLException m) {
            throw new IllegalArgumentException(m);
        }
    }
    */
    public MBeanClassLoader() throws  CustomMBeanException{
        super(MBeanClassLoader.class.getClassLoader());
        init();
    }
    
    public MBeanClassLoader(final ClassLoader delegatee) throws CustomMBeanException {
        super(delegatee);
        init();
    }
    
    private void init() throws CustomMBeanException{
        try {
            final File mbf      = getDefaultMBeanDirectory();
            this.url = mbf.toURL();
        } catch (final MalformedURLException m) {
            String msg = CMBStrings.get("cmb.classloader.init", m.getLocalizedMessage());
            throw new CustomMBeanException(msg, m);
        }        
    }
    
    private File getDefaultMBeanDirectory() {
        /* Ideally, PEFileLayout should be utilized here which should be the central place
         * to get all these paths, locations. But that is too cryptic to find out. Hence
         * I am relying on the properties that are passed to the VM at startup.
         * These properties include the SystemPropertyConstants.INSTANCE_ROOT which always
         * points to where the server instance or domain configuration is stored.
         */
        final File appsFolder = new File(System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY), PEFileLayout.APPLICATIONS_DIR);
        return ( new File(appsFolder, PEFileLayout.MBEAN_FOLDER_NAME) );
    }
    protected Class findClass(final String className) throws ClassNotFoundException {
        final byte[] cd = getClassData(className);
        if (cd == null)
            throw new ClassNotFoundException(CMBStrings.get("findClassFailed", className));
        return ( defineClass(className, cd, 0, cd.length) );
    }

    protected URL findResource(String name)
  {
        File searchResource = new File(url.getFile(), name);
        URL result = null;
        
        if ( searchResource.exists() )
        {
            try
            {
                return searchResource.toURL();
            }
            catch (MalformedURLException mfe)
            {
            }
        }
        return result;
  }
    
    
    private byte[] getClassData(final String cn) {
        byte[] cd = null;
        try {
            if(isURLFile()) {
                cd = getClassDataFromFile(url.getFile(), cn);
            } else {
                cd = getClassDataFromURL();
            }
        } catch(final Exception e) {
            // the caller will throw a ClassNotFoundException
        }
        return ( cd );
    }
    private boolean isURLFile() {
        return ( "file".equals(url.getProtocol()) );
    }
    
    private byte[] getClassDataFromFile(final String base, final String cn) throws IOException, FileNotFoundException {
        String path                         = base + '/' + cn.replace('.', '/') + ".class";
        path                                = prunePath(path);
        final FileInputStream fis           = new FileInputStream(path);
        final BufferedInputStream bis       = new BufferedInputStream(fis);
        final ByteArrayOutputStream bos     = new ByteArrayOutputStream();
        byte[] cd = null;
        try {
            byte[] buf = new byte[1024]; // reading several bytes
            int br = bis.read(buf, 0, buf.length);
            while (br != -1) {
                bos.write(buf, 0, br);
                br = bis.read(buf, 0, buf.length);
            }
            cd = bos.toByteArray();
        } catch(final FileNotFoundException fe) {
            throw fe; 
        } catch (final IOException e) {
            throw e;
        } finally {
            try {
                //This is the single most important item in loading the classes!
                bis.close();
                bos.close();
            } catch(Throwable t) {}
            return ( cd );
        }
    }
    private String prunePath(final String path) {
        /* don't know why url.toFile has got a leading '/' on Windows */
        if (OS.isWindows() && path.charAt(0) == '/') {
            return ( path.substring(1) );
        }
        else
            return ( path ) ;
    }
    private byte[] getClassDataFromURL() {
        throw new UnsupportedOperationException(CMBStrings.get("InternalError", "getClassDataFromURL() -- not implemented yet"));
    }
    ///// Private Methods /////
}
