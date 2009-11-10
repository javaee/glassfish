package org.glassfish.flashlight.impl.core;

/**
 * @author Mahesh Kannan
 *         Date: Nov 8, 2009
 */

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;
import org.objectweb.asm.*;

import java.io.InputStream;
import java.security.ProtectionDomain;
import java.util.logging.Logger;
import java.util.logging.Level;


public class ProviderSubClassImplGenerator {

    private static final Logger logger =
            LogDomains.getLogger(ProviderSubClassImplGenerator.class, LogDomains.MONITORING_LOGGER);
    public final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ProviderSubClassImplGenerator.class);

    private String invokerId;

    private Class providerClazz;

    public ProviderSubClassImplGenerator(Class providerClazz, String invokerId) {
        this.providerClazz = providerClazz;
        this.invokerId = invokerId;
    }

    public <T> Class<T> generateAndDefineClass(Class<T> providerClazz, String generatedClassName, String invokerId) {

        String providerClassName = providerClazz.getName().replace('.', '/');
        byte[] provClassData = null;
        try {
            InputStream is = providerClazz.getClassLoader().getResourceAsStream(providerClassName + ".class");
            int sz = is.available();
            provClassData = new byte[sz];
            int index = 0;
            while (index < sz) {
                int r = is.read(provClassData, index, sz - index);
                if (r > 0) {
                    index += r;
                }
            }
        } catch (Exception ex) {
            return null;
        }

        ClassReader cr = new ClassReader(provClassData);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        byte[] classData = null;
        ProbeProviderSubClassGenerator sgen = new ProbeProviderSubClassGenerator(cw,
                providerClassName, invokerId);
        cr.accept(sgen, 0);
        classData = cw.toByteArray();

        ProtectionDomain pd = providerClazz.getProtectionDomain();

        SubClassLoader scl = new SubClassLoader(providerClazz.getClassLoader());
        try {
            scl.defineClass(generatedClassName, classData, pd);

            System.out.println("**** DEFINE CLASS SUCCEEDED for " + generatedClassName);
            return (Class<T>) scl.loadClass(generatedClassName);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    class SubClassLoader
            extends ClassLoader {

        SubClassLoader(ClassLoader cl) {
            super(cl);
        }

        void defineClass(String className, byte[] data, ProtectionDomain pd)
                throws Exception {

            className = className.replace('/', '.');
            super.defineClass(className, data, 0, data.length, pd);
        }
    }


    private static class ProbeProviderSubClassGenerator
            extends ClassAdapter {

        String superClassName;
        String token;
        ClassVisitor cv;

        ProbeProviderSubClassGenerator(ClassVisitor cv, String superClassName, String token) {
            super(cv);
            this.cv = cv;
            this.superClassName = superClassName;
            this.token = token;
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, superClassName + token, signature, superClassName, interfaces);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            AnnotationVisitor delegate = super.visitAnnotation(desc, visible);
            if ("Lorg/glassfish/external/probe/provider/annotations/ProbeProvider;".equals(desc)) {
                return new ProbeProviderAnnotationVisitor(delegate, token);
            } else {
                return delegate;
            }
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] strings) {

            if ("<init>".equals(name) && desc.equals("()V")) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, strings);
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassName, "<init>", desc);
                mv.visitInsn(Opcodes.RETURN);
                mv.visitMaxs(1, 1);
                mv.visitEnd();

                return null;
            } else {
            return super.visitMethod(access, name, desc, signature, strings);    //To change body of overridden methods use File | Settings | File Templates.
            }
        }
    }

    private static class ConstructorGenerator
        extends MethodAdapter {

        private MethodVisitor mv;

        ConstructorGenerator(MethodVisitor mv) {
            super(mv);
            this.mv = mv;
        }
    }

    private static class ProbeProviderAnnotationVisitor
            implements AnnotationVisitor {

        private AnnotationVisitor delegate;

        private String token;

        ProbeProviderAnnotationVisitor(AnnotationVisitor delegate, String token) {
            this.delegate = delegate;
            this.token = token;
        }

        @Override
        public void visit(String attrName, Object value) {
            delegate.visit(attrName, ("probeProviderName".equals(attrName) ? value + token : value));
        }

        @Override
        public void visitEnum(String s, String s1, String s2) {
            delegate.visitEnum(s, s1, s2);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String s, String s1) {
            return delegate.visitAnnotation(s, s1);
        }

        @Override
        public AnnotationVisitor visitArray(String s) {
            return delegate.visitArray(s);
        }

        @Override
        public void visitEnd() {
            delegate.visitEnd();
        }
    }

    private void printd(String pstring) {
        logger.log(Level.FINEST, pstring);
    }
}