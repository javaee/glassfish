package org.glassfish.flashlight.transformer;

import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeRegistry;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Mahesh Kannan
 */
public class ProbeProviderClassFileTransformer
        implements ClassFileTransformer {

    private static Instrumentation _inst;

    private static boolean _debug;

    private Class providerClass;

    private Map<String, FlashlightProbe> probes = new HashMap<String, FlashlightProbe>();

    private ClassWriter cw;

    private static final Logger _logger = Logger.getLogger(ProbeProviderClassFileTransformer.class.getName());

    public ProbeProviderClassFileTransformer(Class providerClass) {
        this.providerClass = providerClass;
    }

    public void registerProbe(FlashlightProbe probe) {
        Method m = probe.getProbeMethod();
        if (m == null) {
            try {
                m = probe.getProviderClazz().getDeclaredMethod(probe.getProviderJavaMethodName(),
                        probe.getParamTypes());
                probe.setProbeMethod(m);
            } catch (Exception ex) {
                _logger.log(Level.WARNING, "Error during registration of FlashlightProbe", ex);
            }
        }

        probes.put(probe.getProviderJavaMethodName() + "::" + Type.getMethodDescriptor(m), probe);
    }

    public void transform() {
        try {
            ProbeProviderClassFileTransformer.getInstrumentation();
            if (_inst != null) {
                 _inst.addTransformer(this, true);
                 _inst.retransformClasses(providerClass);
            }
        } catch (Exception e) {
            _logger.log(Level.WARNING, "Error during re-transformation", e);
        } finally {
            if (_inst != null) {
                _inst.removeTransformer(this);
            }
        }

    }

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
                            byte[] classfileBuffer)
            throws IllegalClassFormatException {

        try {
            if (classBeingRedefined == providerClass) {

                cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
                ClassReader cr = new ClassReader(classfileBuffer);
                cr.accept(new ProbeProviderClassVisitor(cw), null, 0);

                classfileBuffer = cw.toByteArray();
                if (_debug) {
                    //ProbeProviderClassFileTransformer.writeFile(className.substring(className.lastIndexOf('/') + 1), classfileBuffer);
                }
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "Error during registration of FlashlightProbe", ex);

        }
        return classfileBuffer;
    }

    private static final String makeKey(String name, String desc) {
        return name + "::" + desc;
    }

    private static final void getInstrumentation() {
        if (_inst == null) {
            try {
                ClassLoader scl = ProbeProviderClassFileTransformer.class.getClassLoader().getSystemClassLoader();
                Class agentMainClass = scl.loadClass("org.glassfish.flashlight.agent.ProbeAgentMain");
                Method mthd = agentMainClass.getMethod("getInstrumentation", null);
                _inst = (Instrumentation) mthd.invoke(null, null);

                _logger.log(Level.INFO, "Successfully got INSTRUMENTATION: " + _inst);
            } catch (Exception e) {
                _logger.log(Level.WARNING, "Error while getting Instrumentation object from ProbeAgentmain",  e);
            }
        }
    }

    private static final void writeFile(String name, byte[] data) {
        FileOutputStream fos = null;
        try {
            File dir = new File("/space/work/v3/trunk/glassfish3/glassfish/flashlight-generated");
            dir.mkdirs();
            fos = new FileOutputStream(new File(dir, name + ".class"));

            fos.write(data);
        } catch (Throwable th) {
            _logger.log(Level.INFO, "Couldn't write the retransformed class data", th);
        } finally {
            try {
                fos.close();
            } catch (Exception ex) {
            }
        }
    }

    private class ProbeProviderClassVisitor
            extends ClassAdapter {

        ProbeProviderClassVisitor(ClassVisitor cv) {
            super(cv);
            for (String methodDesc : probes.keySet()) {
                _logger.log(Level.FINE, "ProbeProviderClassVisitor will visit" + methodDesc);
            }
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

            FlashlightProbe probe = probes.get(makeKey(name, desc));
            if (probe != null) {
                mv = new ProbeProviderMethodVisitor(mv, access, name, desc, probe);
            }

            return mv;
        }
    }

    private class ProbeProviderMethodVisitor
            extends MethodAdapter {

        private FlashlightProbe probe;

        private int access;

        private String name;

        private String desc;

        ProbeProviderMethodVisitor(MethodVisitor mv, int access, String name, String desc, FlashlightProbe probe) {
            super(mv);
            this.probe = probe;

            this.access = access;
            this.name = name;
            this.desc = desc;
        }

        public void visitCode() {
            super.visitCode();

            GeneratorAdapter gen = new GeneratorAdapter(mv, access, name, desc);
            //Add the body
            gen.push(probe.getId());
            gen.loadArgArray();
            gen.invokeStatic(Type.getType(
                    ProbeRegistry.class),
                    org.objectweb.asm.commons.Method.getMethod("void invokeProbe(int, Object[])"));
        }
    }

}
