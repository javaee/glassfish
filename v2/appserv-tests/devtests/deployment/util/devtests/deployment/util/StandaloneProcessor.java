/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * StandaloneProcessor.java
 *
 * Created on January 7, 2005, 9:26 AM
 */

package devtests.deployment.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.*;

import javax.enterprise.deploy.shared.ModuleType;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;

import com.sun.enterprise.deployment.annotation.AnnotatedElementHandler;
import com.sun.enterprise.deployment.annotation.AnnotationHandler;
import com.sun.enterprise.deployment.annotation.AnnotationProcessor;
import com.sun.enterprise.deployment.annotation.ProcessingContext;
import com.sun.enterprise.deployment.annotation.ProcessingResult;
import com.sun.enterprise.deployment.annotation.Scanner;
import com.sun.enterprise.deployment.annotation.context.AppClientContext;
import com.sun.enterprise.deployment.annotation.context.EjbBundleContext;
import com.sun.enterprise.deployment.annotation.context.WebBundleContext;
import com.sun.enterprise.deployment.annotation.factory.SJSASFactory;
import com.sun.enterprise.deployment.annotation.impl.AnnotationUtils;
import com.sun.enterprise.deployment.annotation.impl.AppClientScanner;
import com.sun.enterprise.deployment.annotation.impl.DirectoryScanner;
import com.sun.enterprise.deployment.annotation.impl.EjbJarScanner;
import com.sun.enterprise.deployment.annotation.impl.WarScanner;

import com.sun.enterprise.deployment.io.AppClientDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.EjbDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.WebDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.WebServicesDeploymentDescriptorFile;

import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.util.AppClientVisitor;
import com.sun.enterprise.deployment.util.EjbBundleValidator;
import com.sun.enterprise.deployment.util.EjbBundleVisitor;
import com.sun.enterprise.deployment.util.WebBundleVisitor;

/**
 *
 * @author dochez
 */
public class StandaloneProcessor {
    private BundleDescriptor bundleDescriptor = null;
    private AnnotatedElementHandler aeHandler = null;
    private ModuleType type = null;
    private Set<String> compClassNames = null;
    
    /** Creates a new instance of StandaloneProcessor */
    public StandaloneProcessor() {
        this(ModuleType.EJB);
    }

    public StandaloneProcessor(ModuleType type) {
        this.type = type;
        if (ModuleType.EJB.equals(type)) {
            bundleDescriptor = new EjbBundleDescriptor();
            aeHandler = new EjbBundleContext(
                    (EjbBundleDescriptor)bundleDescriptor);
           
        } else if (ModuleType.WAR.equals(type)) {
            bundleDescriptor = new WebBundleDescriptor();
            aeHandler = new WebBundleContext(
                    (WebBundleDescriptor)bundleDescriptor);
        } else if (ModuleType.CAR.equals(type)) {
            bundleDescriptor = new ApplicationClientDescriptor();
            aeHandler = new AppClientContext(
                    (ApplicationClientDescriptor)bundleDescriptor);
        } else {
            throw new UnsupportedOperationException(
                    "ModuleType : " + type + " is not supported.");
        }
    }

    public void setComponentClassNames(Set compClassNames) {
        this.compClassNames = compClassNames;
    }
    
    public static void main(String[] args) throws Exception {
        StandaloneProcessor processor = new StandaloneProcessor();
        processor.run(args);
        processor.generateEjbJarXmlFile(".");
        processor.generateWebServicesXmlFile(".");
    }
    
    public int run(String[] args) throws Exception {
        for (String arg : args) {
            System.out.println("Annotation log is set to " + System.getProperty("annotation.log"));
            String logWhat = System.getProperty("annotation.log");
            if (logWhat!=null) {
                AnnotationUtils.setLoggerTarget(logWhat);
                initLogger();
            }            
            AnnotationUtils.getLogger().info("processing " + arg);
            File f = new File(arg);
            if (f.exists()) {
                try {
                    ClassLoader classLoader = null;
                    if (ModuleType.WAR.equals(type)) {
                        classLoader = new URLClassLoader(
                            new URL[] { new File(f, "WEB-INF/classes").toURL() });
                    } else {
                        classLoader = new URLClassLoader(new URL[]{ f.toURL() });
                    }
                    Scanner scanner = null;

                    if (ModuleType.EJB.equals(type)) {
                        EjbBundleDescriptor ejbBundleDesc =
                                (EjbBundleDescriptor)bundleDescriptor;
                        scanner = new EjbJarScanner(f, ejbBundleDesc, classLoader);
                    } else if (ModuleType.WAR.equals(type)) {
                        WebBundleDescriptor webBundleDesc =
                                (WebBundleDescriptor)bundleDescriptor;
                        for (String cname : compClassNames) {
                            WebComponentDescriptor webCompDesc =
                                    new WebComponentDescriptor();
                            webCompDesc.setServlet(true);
                            webCompDesc.setWebComponentImplementation(cname);
                            webBundleDesc.addWebComponentDescriptor(webCompDesc);
                        }
                        scanner = new WarScanner(f, webBundleDesc, classLoader);
                    } else if (ModuleType.CAR.equals(type)) {
                        String mainClassName = compClassNames.iterator().next();
                        ApplicationClientDescriptor appClientDesc = 
                                (ApplicationClientDescriptor)bundleDescriptor;
                        appClientDesc.setMainClassName(mainClassName);
                        scanner = new AppClientScanner(f, appClientDesc, classLoader);
                    }

                    AnnotationProcessor ap = SJSASFactory.getAnnotationProcessor();
                    
                    // if the user indicated a directory for handlers, time to add the                    
                    String handlersDir = System.getProperty("handlers.dir");
                    if (handlersDir!=null) {
                        addHandlers(ap, handlersDir);
                    }
                    
                    ProcessingContext ctx = ap.createContext();
                    ctx.setErrorHandler(new StandaloneErrorHandler());
                    bundleDescriptor.setClassLoader(scanner.getClassLoader());
                    ctx.setProcessingInput(scanner);
                    ctx.pushHandler(aeHandler);
                    ProcessingResult result = ap.process(ctx);
                    if (ModuleType.EJB.equals(type)) {
                        EjbBundleDescriptor ebd = (EjbBundleDescriptor)bundleDescriptor;
                        ebd.visit((EjbBundleVisitor)new EjbBundleValidator());
                    } else if (ModuleType.WAR.equals(type)) {
                        WebBundleDescriptor wbd = (WebBundleDescriptor)bundleDescriptor;
                        wbd.visit((WebBundleVisitor)new ApplicationValidator());
                    } else if (ModuleType.CAR.equals(type)) {
                        ApplicationClientDescriptor acbd = (ApplicationClientDescriptor)bundleDescriptor;
                        acbd.visit((AppClientVisitor)new ApplicationValidator());
                    }
                    // display the result ejb bundle...
                    AnnotationUtils.getLogger().info("Resulting " + bundleDescriptor);

                } catch(Exception e) {
                    e.printStackTrace();
                    throw e;
                }
            }
        }
        return 0;
    }

    public void generateAppClientXmlFile(String dir) throws IOException {
         AppClientDeploymentDescriptorFile appClientdf = new AppClientDeploymentDescriptorFile();
         appClientdf.write(bundleDescriptor, dir);
    
    }

    public void generateEjbJarXmlFile(String dir) throws IOException {
         EjbDeploymentDescriptorFile ejbdf = new EjbDeploymentDescriptorFile();
         ejbdf.write(bundleDescriptor, dir);
    
    }

    public void generateWebXmlFile(String dir) throws IOException {
         WebDeploymentDescriptorFile webdf = new WebDeploymentDescriptorFile();
         webdf.write(bundleDescriptor, dir);
    
    }

    public void generateWebServicesXmlFile(String dir) throws IOException {
         WebServicesDeploymentDescriptorFile ddf = new WebServicesDeploymentDescriptorFile(bundleDescriptor);
         ddf.write(bundleDescriptor, dir);
    }
    
    private void addHandlers(AnnotationProcessor ap, String handlersDir) {
        try {
        System.out.println("Handlers dir set at " + handlersDir);
        DirectoryScanner scanner = new DirectoryScanner(new File(handlersDir));
        Set<Class> elements = scanner.getElements();
        for (Class handlerClass : elements) {
            Class[] interfaces = handlerClass.getInterfaces();
            for (Class interf : interfaces) {
                if (interf.equals(com.sun.enterprise.deployment.annotation.AnnotationHandler.class)) {
                    AnnotationHandler handler = (AnnotationHandler) handlerClass.newInstance();
                    if (AnnotationUtils.shouldLog("handler")) {
                        AnnotationUtils.getLogger().fine("Registering handler " + handlerClass 
                               + " for type " + handler.getAnnotationType());
                    }
                    ap.pushAnnotationHandler(handler);
                }
            }
        }
        } catch(Exception e) {
            AnnotationUtils.getLogger().severe("Exception while registering handlers : " + e.getMessage());
        }
    }
    
    private void initLogger() {
        try {
            FileHandler handler = new FileHandler("annotation.log");
            handler.setFormatter(new LogFormatter());
            handler.setLevel(Level.FINE);
            Logger logger = Logger.global;
            logger.setLevel(Level.FINE);
            logger.addHandler(handler);} catch(Exception e) {
                e.printStackTrace();
            }       
    }
}
