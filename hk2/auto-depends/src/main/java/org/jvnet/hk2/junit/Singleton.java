package org.jvnet.hk2.junit;

import org.glassfish.hk2.classmodel.reflect.*;
import org.glassfish.hk2.classmodel.reflect.impl.AnnotationModelImpl;
import org.glassfish.hk2.classmodel.reflect.util.ParsingConfig;
import org.jvnet.hk2.annotations.InhabitantAnnotation;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Apr 27, 2010
 * Time: 9:13:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class Singleton {

    public Singleton() {
        System.out.println("Singleton created");
        String classPath = System.getProperty("surefire.test.class.path");
        if (classPath==null) {
            classPath = System.getProperty("java.class.path");
        }
        ParsingContext.Builder builder = new ParsingContext.Builder();
        final Set<String> annotations = new HashSet<String>();
        annotations.add("org.jvnet.hk2.annotations.Contract");
        annotations.add("org.jvnet.hk2.annotations.Service");

        builder.config(new ParsingConfig() {
            final Set<String> empty = Collections.emptySet();

            public Set<String> getInjectionTargetAnnotations() {
                return empty;
            }

            public Set<String> getInjectionTargetInterfaces() {
                return annotations;
            }

            public Set<String> getInjectionPointsAnnotations() {
                return empty;
            }
        });

        ParsingContext context = builder.build();
        Parser parser = new Parser(context);

        StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while(st.hasMoreElements()) {
            final String fileName = st.nextToken();
            File f = new File(fileName);
            if (f.exists()) {
                try {
                    parser.parse(f, new Runnable() {
                        public void run() {
                            System.out.println("Finished parsing " + fileName);
                        }
                    });
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            Exception[] faults = parser.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        for (Type t : context.getTypes().getAllTypes()) {
            if (t instanceof AnnotationModel) {
                System.out.println("Found annotation : " + t.getName());
                if (t.getName().equals(InhabitantAnnotation.class.getName())) {
                    AnnotationModel am = (AnnotationModel) t;
                    for (AnnotatedElement ae : am.allAnnotatedTypes()) {
                        System.out.println("inhabitant annotation : " + ae.getName());
                        if (ae instanceof AnnotationModel) {
                            AnnotationModel inhabitantAnnotationType = (AnnotationModel) ae;
                            for (AnnotatedElement inhabitantAnnotation : inhabitantAnnotationType.allAnnotatedTypes()) {
                                System.out.println("Inhabitant : " + inhabitantAnnotation.getName());
                            }
                        }
                    }
                }
            }
        }        
    }
}
