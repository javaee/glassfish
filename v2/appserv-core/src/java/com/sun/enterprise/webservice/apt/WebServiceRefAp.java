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

/*
 * ServiceRefAp.java
 *
 * Created on June 15, 2005, 9:00 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.sun.enterprise.webservice.apt;

import javax.xml.ws.WebServiceRef;
import java.io.File;
import java.lang.Runtime;
import java.lang.Process;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.util.SimpleDeclarationVisitor;

import static com.sun.mirror.util.DeclarationVisitors.*;

import com.sun.enterprise.deployment.backend.ProcessWatcher;

/**
 *
 * @author dochez
 */
public class WebServiceRefAp implements AnnotationProcessor {
    
    private final AnnotationProcessorEnvironment env;
    // should be set in the context of the appserver, but not in the 
    // apt context

    //let's make this private and see if anyone tries to set it.
    //if we see a linkage error somewhere, the caller will have
    //to use the setLogger method.
    private static Logger logger = null;
   
    public synchronized static void setLogger(Logger l) {
	logger = l;
    }
 
    public WebServiceRefAp(AnnotationProcessorEnvironment env) {
        this.env = env;
    }
    
    public void process() {
        for (TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations())
            typeDecl.accept(getDeclarationScanner(new ServiceRefVisitor(),
                NO_OP));
    }
    
    private class ServiceRefVisitor extends SimpleDeclarationVisitor {
        public void visitClassDeclaration(ClassDeclaration d) {
            if (logger!=null) {
                logger.fine("Processing " + d.getQualifiedName());
            }
        }
        
        public void visitFieldDeclaration(FieldDeclaration f) {
            
            WebServiceRef ref = f.getAnnotation(WebServiceRef.class);
            if (ref==null) {
                return;
            } else {
                if (logger!=null && logger.isLoggable(Level.FINE)) {
                    logger.fine("Found " + f.getSimpleName() + " annotated with WebServiceRef");
                    logger.fine("Name is " + ref.name());
                    logger.fine("WSDL is " + ref.wsdlLocation());
                    logger.fine("of type " + f.getType().toString());  
                }
            }
                
            // no wsdl specified, nothing we can do at this point...
            if (ref.wsdlLocation()==null || ref.wsdlLocation().length()==0) 
                return;
            
            File server = new File(System.getProperty("com.sun.aas.installRoot"));
            server = new File(server, "bin");
            File wsimport = new File(server, "wsimport");
            
            if (wsimport.exists()) {
                
                File classesDir = new File(System.getProperty("user.dir"));
                String outputDir = env.getOptions().get("-d");
                if (outputDir!=null) {
                    if (!(new File(outputDir).isAbsolute())) {
                        classesDir = new File(classesDir, outputDir);
                    }
                    classesDir.mkdirs();
                }
                
                String wsc = wsimport.getAbsolutePath();
                String wscompileArgs = 
                        " -keep -d " + classesDir.getAbsolutePath() +
                        " " + ref.wsdlLocation();    
                if (logger!=null) {
                    logger.log(Level.INFO, "Invoking wsimport with " + ref.wsdlLocation());
                } else {
                    System.out.println("Invoking " + wsimport.getAbsolutePath() + " with " + ref.wsdlLocation() + "in " + classesDir.getAbsolutePath());
                }
                String command = wsc + " " + wscompileArgs;
                if (logger!=null && logger.isLoggable(Level.FINE)) 
                    logger.fine("AdminCommand " + command);
                
                int exitValue=-1;
                try {
                    Process p = Runtime.getRuntime().exec(command);
                    ProcessWatcher pw = new ProcessWatcher(p);
                    exitValue = pw.watch();
                } catch(Exception e) {
                    e.printStackTrace();
                }
                if (exitValue==0) {
                    if (logger!=null)
                        logger.log(Level.INFO, "wsimport successful");
                } else {
                    if (logger!=null)
                        logger.log(Level.SEVERE, "wsimport failed");
                    return;
                }
                
            } else {
                if (logger!=null) {
                    logger.log(Level.SEVERE, "Cannot find wsimport tool");
                } else {
                     System.out.println("Cannot find wsimport");
                }
                return;
            }
        }
            
    }
}  
