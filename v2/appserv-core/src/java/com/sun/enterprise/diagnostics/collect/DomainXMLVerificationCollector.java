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

import com.sun.enterprise.diagnostics.Defaults;
import com.sun.enterprise.diagnostics.Data;
import com.sun.enterprise.diagnostics.Constants;
import com.sun.enterprise.diagnostics.DiagnosticException;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.File;
import java.lang.reflect.*;
import java.io.PrintStream;
import java.io.FileOutputStream;

//import com.sun.enterprise.config.serverbeans.validation.DomainXmlVerifier;
//import com.sun.enterprise.config.serverbeans.validation



/**
 * Captures output of verify-domain-xml command.
 * @author mu125243
 */
public class DomainXMLVerificationCollector implements Collector {
    private String xmlFile;
    private String destFolder;
    /** 
     * Creates a new instance of DomainXMLVerificationCollector 
     * @param destFolder destination folder in which outpur of verify-domain-xml
     * command     
     */
    public DomainXMLVerificationCollector(String repository, 
            String destFolder) {
        this.destFolder = destFolder;
        this.xmlFile = repository + Constants.DOMAIN_XML;
    }
    
    /**
     * Captures output
     * @throw DiagnosticException
     */
    public Data capture() throws DiagnosticException {
        if(destFolder != null) {
            File destFolderObj = new File(destFolder);
            String destFile = destFolder + 
                    Defaults.DOMAIN_XML_VERIFICATION_OUTPUT;
            PrintStream out = System.out;

            if(!destFolderObj.exists()) {
                destFolderObj.mkdirs();
            }
            
            try {
                out = new PrintStream(
                    new BufferedOutputStream(new FileOutputStream(destFile)), true);
            } catch(FileNotFoundException fnfe) {
                System.out.println(" File Not Found Exception ");
                //ignore as output stream is set to System.out
                fnfe.printStackTrace();
            }

            try {
                String className = "com.sun.enterprise.config.serverbeans.validation.DomainXmlVerifier";
                Class classObj = Class.forName(className);
                
                Constructor[] constructors = classObj.getDeclaredConstructors();
                constructors = classObj.getConstructors(); 
                Constructor constructor = 
                        classObj.getConstructor(
                        new Class[]{String.class, PrintStream.class});
                        
                Object obj = constructor.newInstance(new Object[]{xmlFile,out});
                Method method = classObj.getMethod("invokeConfigValidator",(java.lang.Class[]) null);
                method.invoke(obj, (java.lang.Object[] ) null);
                return new FileData(new File(destFile).getName(),
                        DataType.DOMAIN_VALIDATION_DETAILS);
              
            } catch (Exception  ce) {
                Throwable cause = ce.getCause();
                while(cause!=null && !(cause instanceof org.xml.sax.SAXParseException))
                    cause = cause.getCause();
                if(cause!=null)
                    out.println("XML: "+cause.getMessage());
                else
                    ce.printStackTrace();
                throw new DiagnosticException(ce.getMessage());
            }finally {
                out.flush();
                out.close();
            }
        }
        return null;   
    }
}
