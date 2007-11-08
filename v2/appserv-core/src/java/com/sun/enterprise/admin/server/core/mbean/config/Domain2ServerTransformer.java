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

package com.sun.enterprise.admin.server.core.mbean.config;

/* JDK imports */
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.BufferedInputStream;

/* S1AS classes */
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.admin.util.TokenValue;
import com.sun.enterprise.admin.util.TokenValueSet;
import com.sun.enterprise.admin.util.LineTokenReplacer;
import com.sun.enterprise.server.Constants;

/* Transform classes */
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

/**
 * Class to convert domain.xml into server.xml.
 * Currently has the final implementation and has a single method 
 * called "transform".
*/
public final class Domain2ServerTransformer {

    private static final String ORIG_EXT                = ".orig";
    private static final String XSL_NAME                = "domain2server.xsl";
    private static final String DOCTYPE_TOKEN           = "DTDREF";
    private static final String SERVER_DTD_NAME         = "sun-server_1_0.dtd";


    private static final String SERVER_DTD_PATH =
            System.getProperty(Constants.INSTALL_ROOT) +
            "/lib/dtds/" +
            SERVER_DTD_NAME;
    private static String XSL_PATH = 
            System.getProperty(Constants.INSTALL_ROOT) +
            "/lib/install/templates/" +
            XSL_NAME;

    private TransformerFactory          tFactory;
    
    private final String                domainXmlPath;
    private final String                serverXmlPath;
    private final String                origServerXmlPath;

    public Domain2ServerTransformer(String domainXmlPath, 
                                    String serverXmlPath) {
        this.domainXmlPath = domainXmlPath;
        this.serverXmlPath = serverXmlPath;
        origServerXmlPath  = serverXmlPath + ORIG_EXT;
        createFactory();
    }

    private void createFactory() {
        try {
            tFactory = TransformerFactory.newInstance();
            System.out.println("Created xform factory = " + tFactory);
        }
        catch(Exception e) {
            System.out.println("Exception while creating transformer factory");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    public final void transform() {
        this.transform(true);
    }

    public final void transform(boolean backup) {
        if (backup) {
            try {
                if (new File(serverXmlPath).exists()) {
                    FileUtils.copy(serverXmlPath, origServerXmlPath);
                }
                System.out.println("ServerXmlPath = " + serverXmlPath);
                System.out.println("DomainXmlPath = " + domainXmlPath);
                System.out.println("ServerDtdPath = " + SERVER_DTD_PATH);
                System.out.println("XslPath = " + XSL_PATH);
            }
            catch (Exception e) {
                System.out.println("Could not backup server.xml before xform");
                throw new RuntimeException(e);
            }
        }
        final String dtdPath = getDtdPathForServerXml();
        convert();
        replaceDocTypePath(dtdPath);
    }
    
    private void convert() {
        try {
            /* xsl */
            final InputStream  xslStream    = getXslStream();
            final StreamSource xsl          = new StreamSource(xslStream);

            /* domain xml */
            final File domainXmlFile        = new File(domainXmlPath);
            System.out.println("The source xml = " + domainXmlFile.getAbsolutePath());
            final StreamSource xml          = new StreamSource(domainXmlFile);
            
            /* server xml */
            final File serverXmlFile        = new File(serverXmlPath);
            final StreamResult out          = new StreamResult(serverXmlFile); 

            final Transformer transformer   = tFactory.newTransformer(xsl);
            
            transformer.transform(xml, out);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    
    private InputStream getXslStream() throws FileNotFoundException {
        return ( new FileInputStream(XSL_PATH) );
        /*
        final Class thisClass = Domain2ServerTransformer.class;
        final String packageName = 
                thisClass.getPackage().getName();
        final String modifiedName = packageName.replace('.', '/');
        final String xslStreamName = modifiedName + "/" + XSL_NAME;
        System.out.println("The xslStreamName = " + xslStreamName);
        return (thisClass.getClassLoader().getResourceAsStream(xslStreamName));
        */
    }
    
    private void replaceDocTypePath(String dtdPath) {
        try {

            final File tmpFile          = File.createTempFile("temp", ".xml");
            final String tmpFilePath    = tmpFile.getAbsolutePath();
            FileUtils.copy(serverXmlPath, tmpFilePath);

            final TokenValue tv = new TokenValue(DOCTYPE_TOKEN, dtdPath);
            System.out.println("TV = " + tv);
            final TokenValueSet ts = new TokenValueSet();
            ts.add(tv);
            final LineTokenReplacer replacer = new LineTokenReplacer(ts);
            replacer.replace(tmpFilePath, serverXmlPath);
            tmpFile.delete();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    private String getDtdPathForServerXml() {
        /* unfortunately, this has to be formed 
         * by the standard technique of the string concat!
        */
		String PREFIX = "file://";
        if (OS.isWindows()) {
            PREFIX = PREFIX+"/";
        }
        return ( PREFIX + SERVER_DTD_PATH );
        /*
        BufferedInputStream bis = null;
        String dtd = null;
		try {
			final String PREFIX = "file:///";
            final String DTD_EXT = ".dtd";
            final File f = new File(serverXmlPath);
            bis = new BufferedInputStream 
                    (new FileInputStream(f));
            byte[] bytes = new byte[1024];
            int bytesRead = 0;
			while((bytesRead = bis.read(bytes)) != -1) {
                final String line = new String(bytes);
				final int start = line.indexOf(PREFIX);
				if (start != -1) {
					final int end = line.lastIndexOf(DTD_EXT);
					dtd = line.substring(start, end);
                    dtd = dtd + DTD_EXT;
					break;
				}
			}
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if (bis != null)
                    bis.close();
            }
            catch(Exception e) {}
        }
        return ( dtd );
        */
    }
}
