package org.glassfish.admin.runtime.infrastructure.management;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

public class InterfaceBuilder {
    private static final int CONSTANT_Utf8 = 1, CONSTANT_Class = 7;

    public static void oldmain(String[] args) throws Exception {
        Method m1 = Object.class.getMethod("hashCode");
        Method m2 = InterfaceBuilder.class.getMethod("buildInterface", String.class, Method[].class);
        Method m3 = InterfaceBuilder.class.getDeclaredMethod("build", String.class, Method[].class);
        Method m4 = Runnable.class.getMethod("run");
        XMethod[] methods = {new XMethod(m1), new XMethod(m2), new XMethod(m3), new XMethod(m4)};
        byte[] bytes = buildInterface("HashCode", methods);
        FileOutputStream fout = new FileOutputStream("HashCode.class");
        fout.write(bytes);
        fout.close();
        InterfaceClassLoader cl =
                new InterfaceClassLoader(InterfaceBuilder.class.getClassLoader());
        Class<?> c = cl.findOrBuildInterface("HashCode", methods);
        Object proxy =
                Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[] {c},
                                       new PrintInvocationHandler());
        proxy.hashCode();
        Method m2x = c.getMethod("buildInterface", String.class, Method[].class);
        m2x.invoke(proxy, "yo", new Method[] {m2, m3});
        c = cl.findOrBuildInterface("Bang", new XMethod[] {new XMethod(m4)});
        proxy = Proxy.newProxyInstance(c.getClassLoader(), new Class<?>[] {c},
                                       new ExceptionInvocationHandler());
        Method m4x = c.getMethod("run");
        m4x.invoke(proxy);
    }
    
    /*
    public static void main(String[] args) throws Exception {
        MBeanBuilder mbeanBuilder =
                new MBeanBuilder(InterfaceBuilder.class.getClassLoader());
        XStandardMBean mbean = mbeanBuilder.buildMBean(new Test1());
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(mbean, new ObjectName("a:b=c"));
        System.out.println("Ready");
        System.in.read();
    }
    */
    
    public static void createMBean(Object rtaObject, String objectName) throws Exception {
        MBeanBuilder mbeanBuilder =
                new MBeanBuilder(InterfaceBuilder.class.getClassLoader());
        XStandardMBean mbean = mbeanBuilder.buildMBean(rtaObject);
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbs.registerMBean(mbean, new ObjectName(objectName));
    }
    
    private static class PrintInvocationHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args) {
            System.out.print(method.getName() + "(");
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (i > 0)
                        System.out.print(", ");
                    if (args[i] instanceof Object[])
                        System.out.print(Arrays.toString((Object[]) args[i]));
                    else
                        System.out.print(args[i]);
                }
            }
            System.out.println(")");
            return defaultValue(method.getReturnType());
        }
        
        private static Object defaultValue(Class<?> c) {
            if (!c.isPrimitive())
                return null;
            return defaultValues.get(c);
        }
        
        private static final Map<Class<?>, Object> defaultValues =
                new HashMap<Class<?>, Object>();
        static {
            defaultValues.put(int.class, 0);
            defaultValues.put(long.class, 0L);
            defaultValues.put(byte.class, (byte) 0);
            defaultValues.put(short.class, (short) 0);
            defaultValues.put(boolean.class, false);
            defaultValues.put(char.class, '\0');
            defaultValues.put(float.class, 0f);
            defaultValues.put(double.class, 0.0);
        }
    }

    private static class ExceptionInvocationHandler implements InvocationHandler {
        public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
            throw new Exception("Exception!");
        }
    }
    
    /**
     * Return the byte code for an interface called {@code name} that
     * contains the given {@code methods}.  Every method in the generated
     * interface will be declared to throw {@link Exception}.
     */
    public static byte[] buildInterface(String name, XMethod[] methods) {
        try {
            return new InterfaceBuilder().build(name, methods);
        } catch (IOException e) {
            // we're only writing arrays, so this "can't happen"
            throw new RuntimeException(e);
        }
    }

    private InterfaceBuilder() {
    }

    private byte[] build(String name, XMethod[] methods) throws IOException {
	ByteArrayOutputStream bout = new ByteArrayOutputStream();
	DataOutputStream dout = new DataOutputStream(bout);

        dout.writeInt(0xcafebabe);     // u4 magic
        dout.writeShort(0);            // u2 minor_version
        dout.writeShort(45);           // u2 major_version (Java 1.0.2)

        byte[] afterConstantPool = buildAfterConstantPool(name, methods);

        writeConstantPool(dout);
        dout.write(afterConstantPool);
        return bout.toByteArray();
    }

    private byte[] buildAfterConstantPool(String name, XMethod[] methods)
    throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);

        dout.writeShort(Modifier.PUBLIC|Modifier.INTERFACE|Modifier.ABSTRACT);
                                         // u2 access_flags
        dout.writeShort(classConstant(name));
                                         // u2 this_class
        dout.writeShort(classConstant(Object.class.getName()));
                                         // u2 super_class
        dout.writeShort(0);              // u2 interfaces_count
        dout.writeShort(0);              // u2 fields_count

        dout.writeShort(methods.length); // u2 methods_count
        for (int i = 0; i < methods.length; i++) {
            dout.writeShort(Modifier.PUBLIC|Modifier.ABSTRACT);
                                              // u2 access_flags
            dout.writeShort(stringConstant(methods[i].getName()));
                                              // u2 name_index
            dout.writeShort(stringConstant(methodDescriptor(methods[i])));
                                              // u2 descriptor_index
            dout.writeShort(1);               // u2 attributes_count
            dout.writeShort(stringConstant("Exceptions"));
                                                    // u2 attribute_name_index
            dout.writeInt(4);                       // u4 attribute_length:
            dout.writeShort(1);                     // (u2 number_of_exceptions
            dout.writeShort(classConstant(Exception.class.getName()));
                                                    //  + u2 exception_index)
        }

        dout.writeShort(0);              // u2 attributes_count (for class)
        return bout.toByteArray();
    }

    private String methodDescriptor(XMethod method) {
        StringBuilder sb = new StringBuilder("(");
        for (Class<?> param : method.getParameterTypes())
            sb.append(classCode(param));
        sb.append(")").append(classCode(method.getReturnType()));
        return sb.toString();
    }

    private String classCode(Class<?> c) {
        if (c == void.class)
            return "V";
        Class<?> arrayClass = Array.newInstance(c, 0).getClass();
        return arrayClass.getName().substring(1).replace('.', '/');
    }

    private int stringConstant(String s) {
        return constant(CONSTANT_Utf8, s);
    }

    private int classConstant(String s) {
        int classNameIndex = stringConstant(s.replace('.', '/'));
        return constant(CONSTANT_Class, classNameIndex);
    }

    private int constant(Object... data) {
        List<?> dataList = Arrays.asList(data);
        if (poolMap.containsKey(dataList))
            return poolMap.get(dataList);
        poolMap.put(dataList, poolIndex);
        return poolIndex++;
    }

    private void writeConstantPool(DataOutputStream dout) throws IOException {
        dout.writeShort(poolIndex);
        int i = 1;
        for (List<?> data : poolMap.keySet()) {
            assert(poolMap.get(data).equals(i++));
            int tag = (Integer) data.get(0);
            dout.writeByte(tag);          // u1 tag
            switch (tag) {
                case CONSTANT_Utf8:
                    dout.writeUTF((String) data.get(1));
                    break;                // u2 length + u1 bytes[length]
                case CONSTANT_Class:
                    dout.writeShort((Integer) data.get(1));
                    break;                // u2 name_index
                default:
                    throw new AssertionError();
            }
        }
    }

    private final Map<List<?>, Integer> poolMap =
            new LinkedHashMap<List<?>, Integer>();
    private int poolIndex = 1;
}
