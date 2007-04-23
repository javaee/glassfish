/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package com.sun.enterprise.tools.apt;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundCompleteListener;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.DeclarationVisitors;
import com.sun.mirror.util.SimpleDeclarationVisitor;
import org.jvnet.hk2.annotations.Contract;
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
 * This class is proccessing the @Service annotation and generates
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

        for (TypeDeclaration typeDecl : env.getSpecifiedTypeDeclarations())
            typeDecl.accept(DeclarationVisitors.getDeclarationScanner(new ListClassVisitor(),
                    DeclarationVisitors.NO_OP));
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

        }

        private void checkContract(TypeDeclaration type, ClassDeclaration impl) {
            Contract contract = type.getAnnotation(Contract.class);
            if (contract != null) {

                ServiceFileInfo info;
                if (!serviceFiles.containsKey(type.getQualifiedName())) {
                    info = new ServiceFileInfo(type.getQualifiedName(), new HashSet<String>());
                    serviceFiles.put(type.getQualifiedName(), info);
                } else {
                    info = serviceFiles.get(type.getQualifiedName());
                }

                if (!info.getImplementors().contains(impl.getQualifiedName())) {
                    info.getImplementors().add(impl.getQualifiedName());
                    try {
                        info.createFile(env);
                    } catch(IOException ioe) {
                        env.getMessager().printError(ioe.getMessage());
                    }

                }
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
        final boolean debug = env.getOptions().containsKey("-Adebug");
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
