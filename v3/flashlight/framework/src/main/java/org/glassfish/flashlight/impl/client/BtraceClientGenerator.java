package org.glassfish.flashlight.impl.client;

/**
 * @author Mahesh Kannan
 *         Date: Jul 20, 2008
 */

import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeRegistry;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedActionException;
import java.security.ProtectionDomain;
import java.util.Collection;

public class BtraceClientGenerator {

    private BtraceClientGenerator() {
        // all static class
        // no instances allowed
    }

    public static byte[] generateBtraceClientClassData(int clientID,
    		Collection<FlashlightProbe> probesRequiringTransformation) {

        StringBuilder sb = new StringBuilder("com.sun.btrace.flashlight.");
        /*
        // create a unique name.  It does not matter what the name is.
        for (FlashlightProbe probe : probesRequiringTransformation) {
            sb.append(probe.getProbeName());
            sb.append("_");
        }
         */

        sb.append("BTrace_").append(clientID);
        String generatedClassName = sb.toString();
        generatedClassName = generatedClassName.replace('.', '/');
        int cwFlags = ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS;
        ClassWriter cw = new ClassWriter(cwFlags);

        int access = Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL;
        cw.visit(Opcodes.V1_5, access, generatedClassName, null, 
        		"java/lang/Object", null);
        AnnotationVisitor av = cw.visitAnnotation("Lcom/sun/btrace/annotations/BTrace;", true);

        Type probeType = Type.getType(FlashlightProbe.class);
        int methodCounter = 0;
        for (FlashlightProbe probe : probesRequiringTransformation) {
        	//System.out.println("BTraceGen: Generating method[" + methodCounter + "] => " + probe);
            String typeDesc = "void ";
            String methodDesc = "void __"
            	+ probe.getProviderJavaMethodName() + "__"
            	+ clientID + "_" + methodCounter + "_";
            methodDesc += "(";
            typeDesc += "(";
            String delim = "";
            for (Class paramType : probe.getParamTypes()) {
                methodDesc += delim + paramType.getName();
                typeDesc += delim + paramType.getName();
                delim = ", ";
            }
            methodDesc += ")";
            typeDesc += ")";
            Method m = Method.getMethod(methodDesc);
            GeneratorAdapter gen = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, m, null, null, cw);
            av = gen.visitAnnotation("Lcom/sun/btrace/annotations/OnMethod;", true);
            av.visit("clazz", "" + probe.getProviderClazz().getName());
            av.visit("method", probe.getProviderJavaMethodName());
            av.visit("type", typeDesc);
            av.visitEnd();

            gen.push(probe.getId());
            gen.loadArgArray();
            
            gen.invokeStatic(Type.getType(
            		ProbeRegistry.class), Method.getMethod("void invokeProbe(int, Object[])"));
            gen.returnValue();
            
            gen.endMethod();
            
            methodCounter++;
            //System.out.println("**** Generated BTraceGen method: " + methodCounter);
        }


        BtraceClientGenerator.generateConstructor(cw);

        cw.visitEnd();

        byte[] classData = cw.toByteArray();
        writeClass(classData, generatedClassName);
        return classData;
    }

    private static void writeClass(byte[] classData, String generatedClassName) {
        // only do this if we are in "debug" mode
        String debug = System.getenv("AS_DEBUG");
        if(debug == null || !debug.equals("true"))
            return;

        System.out.println("**** Generated BTRACE Client " + generatedClassName);

        try {
            int index = generatedClassName.lastIndexOf('/');
            String rootPath = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) +
                                File.separator + "lib" + File.separator;

            String fileName = rootPath + generatedClassName.substring(index+1) + ".class";
            //System.out.println("***ClassFile: " + fileName);
            File file = new File(fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(classData);
            fos.flush();
            fos.close();
        }
        catch (Exception ex) {
           ex.printStackTrace();
        }
    }

    private static void generateConstructor(ClassWriter cw) {
        Method m = Method.getMethod("void <init> ()");
        GeneratorAdapter gen = new GeneratorAdapter(Opcodes.ACC_PUBLIC, m, null, null, cw);
        gen.loadThis();
        gen.invokeConstructor(Type.getType(Object.class), m);

        //return the value from constructor
        gen.returnValue();
        gen.endMethod();
    }
}


/****  Example generated class (bnevins, August 2009)
 *
 * package com.sun.btrace.flashlight.org.glassfish.web.admin.monitor;

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.OnMethod;
import javax.servlet.Servlet;
import org.glassfish.flashlight.provider.ProbeRegistry;

@BTrace
public final class ServletStatsProvider_BTrace_7_
{
  @OnMethod(clazz="org.glassfish.web.admin.monitor.ServletProbeProvider", method="servletInitializedEvent", type="void (javax.servlet.Servlet, java.lang.String, java.lang.String)")
  public static void __servletInitializedEvent__7_0_(Servlet paramServlet, String paramString1, String paramString2)
  {
    ProbeRegistry.invokeProbe(78, new Object[] { paramServlet, paramString1, paramString2 });
  }

  @OnMethod(clazz="org.glassfish.web.admin.monitor.ServletProbeProvider", method="servletDestroyedEvent", type="void (javax.servlet.Servlet, java.lang.String, java.lang.String)")
  public static void __servletDestroyedEvent__7_1_(Servlet paramServlet, String paramString1, String paramString2)
  {
    ProbeRegistry.invokeProbe(79, new Object[] { paramServlet, paramString1, paramString2 });
  }
}
 */