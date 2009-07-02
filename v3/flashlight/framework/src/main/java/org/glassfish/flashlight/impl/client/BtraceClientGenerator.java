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

	/*
    public String defineClass(FlashlightProbeProvider provider, Class providerClazz) {

        String generatedClassName = provider.getModuleProviderName() + "_Flashlight_" + provider.getModuleName() + "_"
                + "Probe_" + ((provider.getProbeProviderName() == null) ? providerClazz.getName() : provider.getProbeProviderName());
        generatedClassName = providerClazz.getName() + "_" + generatedClassName;

        byte[] classData = generateBtraceClientClassData(provider, providerClazz, generatedClassName);

        ProtectionDomain pd = providerClazz.getProtectionDomain();

        java.lang.reflect.Method jm = null;
        for (java.lang.reflect.Method jm2 : ClassLoader.class.getDeclaredMethods()) {
            if (jm2.getName().equals("defineClass") && jm2.getParameterTypes().length == 5) {
                jm = jm2;
                break;
            }
        }

        final java.lang.reflect.Method clM = jm;
        try {
            java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedExceptionAction() {
                        public java.lang.Object run() throws Exception {
                            if (!clM.isAccessible()) {
                                clM.setAccessible(true);
                            }
                            return null;
                        }
                    });

            clM.invoke(providerClazz.getClassLoader(), generatedClassName, classData, 0,
                    classData.length, pd);

            return generatedClassName;
        } catch (PrivilegedActionException pEx) {
            throw new RuntimeException(pEx);
        } catch (IllegalAccessException
                illegalAccessException) {
            throw new RuntimeException(illegalAccessException);
        } catch (InvocationTargetException
                invtEx) {
            throw new RuntimeException(invtEx);
        }

    }
    */

    public static byte[] generateBtraceClientClassData(int clientID,
    		Collection<FlashlightProbe> probesRequiringTransformation,
    		Class clientClazz) {


        Type classType = Type.getType(clientClazz);
        //System.out.println("** classType: " + classType);
        //System.out.println("** classDesc: " + Type.getDescriptor(providerClazz));

        //System.out.println("Generating for: " + generatedClassName);

        String generatedClassName = clientClazz.getName();
        generatedClassName = "BTrace_" + clientID + "_"
        	+ generatedClassName.replace('.', '/');

        int cwFlags = ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS;
        ClassWriter cw = new ClassWriter(cwFlags);

        int access = Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL;
        cw.visit(Opcodes.V1_5, access, generatedClassName, null, 
        		"java/lang/Object", null);
        AnnotationVisitor av = cw.visitAnnotation("Lcom/sun/btrace/annotations/BTrace;", true);

        Type probeType = Type.getType(FlashlightProbe.class);
        int methodCounter = 0;
        for (FlashlightProbe probe : probesRequiringTransformation) {
        	//System.out.println("Generating method[" + methodCounter + "] => " + probe);
            String methodDesc = "void __"
            	+ probe.getProviderJavaMethodName() + "__"
            	+ clientID + "_" + methodCounter + "_";
            methodDesc += "(";
            String delim = "";
            for (Class paramType : probe.getParamTypes()) {
                methodDesc += delim + paramType.getName();
                delim = ", ";
            }
            methodDesc += ")";
            Method m = Method.getMethod(methodDesc);
            GeneratorAdapter gen = new GeneratorAdapter(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, m, null, null, cw);
            av = gen.visitAnnotation("Lcom/sun/btrace/annotations/OnMethod;", true);
            av.visit("clazz", "" + probe.getProviderClazz().getName());
            av.visit("method", probe.getProviderJavaMethodName());
            av.visitEnd();

            gen.push(probe.getId());
            gen.loadArgArray();
            
            gen.invokeStatic(Type.getType(
            		ProbeRegistry.class), Method.getMethod("void invokeProbe(int, Object[])"));
            gen.returnValue();
            
            gen.endMethod();
            
            methodCounter++;
        }


        BtraceClientGenerator.generateConstructor(cw);

        cw.visitEnd();

        byte[] classData = cw.toByteArray();
        /*
        System.out.println("**** Generated BTRACE Client " + generatedClassName);
        try {
        	int index = generatedClassName.lastIndexOf('/');
            String rootPath = System.getProperty(SystemPropertyConstants.INSTALL_ROOT_PROPERTY) +
                                File.separator + "lib" + File.separator;

            String fileName = rootPath + generatedClassName.substring(index+1) + ".class";
            System.out.println("***ClassFile: " + fileName);
            File file = new File(fileName);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(classData);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
           ex.printStackTrace();
        }
        */
        return classData;
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
