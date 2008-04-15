/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.tools.apt;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.MirroredTypeException;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.DeclarationVisitors;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is proccessing the @{@link Service} annotation and generates
 * META-INF/services style text file for each interface annotated with @Contract
 *
 * @author Jerome Dochez
 */
public class ServiceAnnotationProcessor implements AnnotationProcessor, RoundCompleteListener {

    private final boolean debug;
    private final AnnotationProcessorEnvironment env;
    private Map<String, ServiceFileInfo> serviceFiles = new HashMap<String, ServiceFileInfo>();

    /**
     * Creates a new instance of ServiceAnnotationProcessor
     */
    public ServiceAnnotationProcessor(AnnotationProcessorEnvironment env) {
        this.env = env;
        env.addListener(this);

        debug = env.getOptions().containsKey("-Adebug");
        if (debug) {
            env.getMessager().printNotice(env.getOptions().toString());
        }
        
        // load all existing meta-inf files...
        loadExistingMetaInfFiles();
    }


    /**
     * Loads all existing META-INF/services file from our destination directory
     * This is usuful because during incremental builds, not all source files
     * are recompiled, henve we cannot rewrite the META-INF/services file from
     * scratch each time, but append/remove entries from it as necessary.
     */
    protected void loadExistingMetaInfFiles() {
        String outDirectory = env.getOptions().get("-s");
        if (outDirectory==null) {
            outDirectory = System.getProperty("user.dir");
        }
        File outDir = new File(new File(outDirectory),"META-INF/services").getAbsoluteFile();
        if (debug) {
            env.getMessager().printNotice("Output dir is " + outDir.getAbsolutePath());
        }

        if (!outDir.exists()) {
            return;
        }
        for (File file : outDir.listFiles()) {
            if(file.isDirectory())  continue;
            Set<String> entries = new HashSet<String>();
            try {
                FileReader reader = new FileReader(file);
                LineNumberReader lineReader = new LineNumberReader(reader);
                String line = lineReader.readLine();
                while (line != null) {
                    entries.add(line);
                    line = lineReader.readLine();
                }
            } catch (IOException e) {
                env.getMessager().printError(e.getMessage());
            }
            ServiceFileInfo info = new ServiceFileInfo(file.getName(), entries);
            serviceFiles.put(file.getName(), info);
        }
    }

    /**
     * Annotation processor entry point, we are using a visitor pattern the visit
     * only the class declaration.
     */
    public void process() {
        DeclarationVisitor scanner = DeclarationVisitors.getDeclarationScanner(
            new ListClassVisitor(),
            DeclarationVisitors.NO_OP);

        for (TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations())
            typeDecl.accept(scanner);
    }

    /**
     * Invoked on all class declaration, use the cached META-INF/servies
     * information and the mirror APIs to find out if classes need to be
     * added or removed from the generated service file.
     */
    private class ListClassVisitor extends SimpleDeclarationVisitor {
        public void visitClassDeclaration(ClassDeclaration d) {
            if (debug) {
                env.getMessager().printNotice("Visiting " + d.getQualifiedName());
            }
            Service service = d.getAnnotation(Service.class);
            if (debug) {
                env.getMessager().printNotice("Service annotation = " + service);
            }
            if (service != null) {

                // look for @ContractProvided interface
                checkContractProvided(d);
                
                // look for contract in interfaces
                for (InterfaceType intf : d.getSuperinterfaces()) {
                    checkContract(intf.getDeclaration(), d);
                }

                // look for contract in super classes
                ClassDeclaration sd = d;
                while(sd.getSuperclass()!=null) {
                    sd = sd.getSuperclass().getDeclaration();
                    checkContract(sd,d);
                }
            } else {
                // we need to check if that class previously add an @Service annotation so
                // we remove the entry from the META-INF file
                for (ServiceFileInfo info : serviceFiles.values()) {
                    if (debug) {
                        env.getMessager().printNotice("Checking against " + info.getServiceName());
                    }
                    for (String implementor : info.getImplementors()) {
                        if (implementor.equals(d.getQualifiedName())) {
                            if (debug) {
                                env.getMessager().printNotice("Need to remove " + implementor);
                            }
                            info.getImplementors().remove(implementor);
                            try {
                                info.createFile(env);
                            } catch(IOException ioe) {
                                env.getMessager().printError(ioe.getMessage());
                            }
                            return;    
                        }
                    }
                }
            }

            // check for meta annotations
            for(AnnotationMirror a : d.getAnnotationMirrors()) {
                AnnotationTypeDeclaration atd = a.getAnnotationType().getDeclaration();
                Contract c = atd.getAnnotation(Contract.class);
                if(c!=null) {
                    // this is a contract annotation
                    createContractImplementation(atd.getQualifiedName(),d);
                }
            }
        }


        private void checkContractProvided(ClassDeclaration impl) {
            ContractProvided provided = impl.getAnnotation(ContractProvided.class);

            String intfName = null;
            if (provided != null) {

                try {
                    provided.value();
                } catch (MirroredTypeException e) {
                    intfName = e.getQualifiedName();
                }
                if (debug) {
                    System.out.println("Provided is " + intfName);
                }
                createContractImplementation(intfName, impl);
            }

        }

        private void checkContract(TypeDeclaration type, ClassDeclaration impl) {
            Contract contract = type.getAnnotation(Contract.class);

            if (contract != null)
                createContractImplementation(type.getQualifiedName(), impl);
        }
    }

    /**
     * Records the contract&lt;->service relationship.
     *
     * @param contractName
     *      FQCN of the contract type/annotation.
     * @param impl
     *      Implementation class.
     */
    private void createContractImplementation(String contractName, ClassDeclaration impl) {
        ServiceFileInfo info;
        if (!serviceFiles.containsKey(contractName)) {
            info = new ServiceFileInfo(contractName, new HashSet<String>());
            serviceFiles.put(contractName, info);
        } else {
            info = serviceFiles.get(contractName);
        }

        if (info.getImplementors().add(impl.getQualifiedName())) {
            try {
                info.createFile(env);
            } catch (IOException ioe) {
                env.getMessager().printError(ioe.getMessage());
            }
        }
    }

    /**
     * Notification of round complete, I use this to actually write the
     * service file content and close the file.
     * 
     * @param e
     */
    public void roundComplete(RoundCompleteEvent e) {
        for (ServiceFileInfo info : serviceFiles.values()) {
            if (info.isDirty()) {
                if (debug) {
                    env.getMessager().printNotice("Creating META-INF/services "+ info.getServiceName() + " file");
                }
                PrintWriter writer = info.getWriter();
                for (String implementor : info.getImplementors()) {
                    if (debug) {
                        env.getMessager().printNotice(" Implementor " + implementor);
                    }
                    writer.println(implementor);
                }
                writer.close();
            }
        }
    }
}
