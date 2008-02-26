package org.glassfish.admin.runtime.infrastructure.management;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;
import com.sun.mirror.type.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;

class RuntimeMgmtAptProcessor implements AnnotationProcessor {

    private final AnnotationProcessorEnvironment env;
    private final Messager msg;
    private final boolean debug;

    public RuntimeMgmtAptProcessor(AnnotationProcessorEnvironment env) {
        this.env = env;
        msg = env.getMessager();
        debug = env.getOptions().containsKey("-Adebug");
    }
    
    public void process() {
        for (TypeDeclaration decl : env.getSpecifiedTypeDeclarations()) {
            if (debug) {
                msg.printNotice("type = " + decl.getQualifiedName());
                msg.printNotice("package = " + decl.getPackage().getQualifiedName());
            }
            generateInterface(decl);
        }
    }

    private void generateInterface(TypeDeclaration decl) {
        if (decl.getAnnotation(MBean.class) == null) return;
        String ifName = (decl.getSimpleName()+"MBean");
        if (debug)
            msg.printNotice("generating interface ..." + ifName);
        try {
            PrintWriter out = env.getFiler().createTextFile(
                Filer.Location.CLASS_TREE, // -d option to javac
                "",
                new File(ifName + ".txt"),
                null);

            out.println("pojo:" + 
                decl.getPackage().getQualifiedName() + "." +
                decl.getSimpleName());

            // generate methods
            for (MethodDeclaration mdecl : decl.getMethods()) {
                generateMethods(mdecl, out);
            }

            out.flush();
            out.close();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private void generateMethods(MethodDeclaration mdecl, PrintWriter out) {
        if ((mdecl.getAnnotation(ManagedOperation.class) == null) &&
            (mdecl.getAnnotation(ManagedAttribute.class) == null)) 
            return;

        String type = null;

        try {
            if (! mdecl.getModifiers().contains(Modifier.PUBLIC)) return;

            if (mdecl.getAnnotation(ManagedOperation.class) != null) {
                type = "operation";
            } else if (mdecl.getAnnotation(ManagedAttribute.class) != null) {
                type = "attribute";
            }
            out.print(type + ":(");


            String methodName = mdecl.getSimpleName();
            out.print(methodName);


            int k = 0;
            for (ParameterDeclaration pdecl : mdecl.getParameters()) {
                out.print(",");
                out.print(pdecl.getType().toString());
                k++;
            }

            out.println(")");


        } catch (Exception ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
