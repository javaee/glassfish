/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elf.asmworkshop;

import java.io.*;
import java.util.*;
import org.objectweb.asm.*;
//import org.objectweb.asm.attrs.*;
public class PrimeProviderDump implements Opcodes {
    public static byte[] dump() throws Exception {

        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0;

        cw.visit(V1_6, ACC_ABSTRACT + ACC_INTERFACE, "PrimeProvider", null, "java/lang/Object", new String[]{"com/sun/tracing/Provider"});
        {
            av0 = cw.visitAnnotation("Lcom/sun/tracing/ProviderName;", true);
            av0.visit("value", "MyProvider");
            av0.visitEnd();
        }
        {
            av0 = cw.visitAnnotation("Lcom/sun/tracing/dtrace/ModuleName;", true);
            av0.visit("value", "MyModule");
            av0.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "divideCheck", "(II)V", null, null);
            {
                av0 = mv.visitAnnotation("Lcom/sun/tracing/dtrace/FunctionName;", true);
                av0.visit("value", "MyFunctionDivide");
                av0.visitEnd();
            }
            {
                av0 = mv.visitAnnotation("Lcom/sun/tracing/ProbeName;", true);
                av0.visit("value", "MyProbeDivideCheck");
                av0.visitEnd();
            }
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "primeCheck", "(I)V", null, null);
            {
                av0 = mv.visitAnnotation("Lcom/sun/tracing/dtrace/FunctionName;", true);
                av0.visit("value", "MyFunctionPrimeCheck");
                av0.visitEnd();
            }
            {
                av0 = mv.visitAnnotation("Lcom/sun/tracing/ProbeName;", true);
                av0.visit("value", "MyProbePrimeCheck");
                av0.visitEnd();
            }
            mv.visitEnd();
        }
        {
            mv = cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "foundPrime", "(Ljava/lang/String;)V", null, null);
            {
                av0 = mv.visitAnnotation("Lcom/sun/tracing/dtrace/FunctionName;", true);
                av0.visit("value", "MyFunctionPrimeFound");
                av0.visitEnd();
            }
            {
                av0 = mv.visitAnnotation("Lcom/sun/tracing/ProbeName;", true);
                av0.visit("value", "MyProbePrimeFound");
                av0.visitEnd();
            }
            mv.visitEnd();
        }
        cw.visitEnd();

        byte[] classData = cw.toByteArray();
        writeClass(classData, "PrimeProvider");
        return classData;
    }

    private static void writeClass(byte[] classData, String generatedClassName) {
        try {
            File f  = new File("c:/work/generated");
            f.mkdirs();
            f = new File(f, generatedClassName + ".class");

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(classData);
            fos.flush();
            fos.close();
        }
        catch (Exception ex) {
           ex.printStackTrace();
        }
    }
}

