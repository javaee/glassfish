package org.glassfish.hk2.utilities.reflection;

import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This member descriptor must have the same equals
 * and hashCode even if the declaringClass is *different*.
 * This allows us to override a super-classes definition
 * 
 * The other point of this class is to have a scalar only
 * representation of a Field or Method so that this will
 * not keep references to classes that may go away.  However, it
 * must also be highly performant, which is why we keep the
 * declaring class, so that we can quickly find the true
 * class where the method or field is defined quickly without
 * searching the entire class hierarchy
 * 
 * Note that when the SoftReference returns null then it is time
 * to clean the cache, as some class or another we had in the cache
 * is now gone
 * 
 * @author jwells
 *
 */
class MemberDescriptor {
    private final ClassReflectionModel classReflectionModel;
    private final String declaringClass;
    private final String name;
    private final String args[];
    private final boolean isMethod;
    private final boolean isPrivate;
    private final int hashCode;
    
    private SoftReference<Member> soft;
    
    MemberDescriptor(ClassReflectionModel classReflectionModel, Member member) {
        this.classReflectionModel = classReflectionModel;
        if (member == null) {
            declaringClass = null;
            name = null;
            args = null;
            isMethod = true;
            isPrivate = false;
            hashCode = 0;
            return;
        }
        
        name = member.getName();
        declaringClass = member.getDeclaringClass().getName();
        isPrivate = ReflectionHelper.isPrivate(member);
        soft = new SoftReference<Member>(member);
        
        if (member instanceof Field) {
            isMethod = false;
            
            args = null;
            
            hashCode = name.hashCode();
        }
        else if (member instanceof Method) {
            isMethod = true;
            
            int calcHashCode = 1 ^ name.hashCode();
            
            Method method = (Method) member;
            
            Class<?> mArgs[] = method.getParameterTypes();
            args = new String[mArgs.length];
            for (int lcv = 0; lcv < mArgs.length; lcv++) {
                args[lcv] = mArgs[lcv].getName();
                calcHashCode ^= args[lcv].hashCode();
            }
            
            hashCode = calcHashCode;
        }
        else {
            throw new AssertionError("Unknown member type " + member);
        }
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof MemberDescriptor)) return false;
        
        MemberDescriptor oMd = (MemberDescriptor) o;
        if (hashCode != oMd.hashCode) return false; 
        
        if (isMethod != oMd.isMethod) {
            // Fields are not equal to methods
            return false;
        }
        
        if (!isMethod) {
            // Fields do not inherit
            return false;
        }
        
        // Null name check
        if (name == null) {
            if (oMd.name != null) return false;
        }
        else if (oMd.name == null) return false;
        
        if (!name.equals(oMd.name)) return false;
        if (isPrivate || oMd.isPrivate) return false;
        
        if (args == null) return true; // This is a field
        
        if (args.length != oMd.args.length) return false;
        
        for (int lcv = 0; lcv < args.length; lcv++) {
            if (!args[lcv].equals(oMd.args[lcv])) return false;
        }
        
        return true;
    }
    
    Field getField(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Field>() {

            @Override
            public Field run() {
                return internalGetField(clazz);
            }
            
        });
    }
    
    private Field internalGetField(Class<?> clazz) {
        if (name == null || isMethod) return null;
        Field retVal = (Field) soft.get();
        if (retVal == null) {
            this.classReflectionModel.cleanCache();
        }
        
        if (ClassReflectionModel.USE_SOFT_REFERENCE && (retVal != null) &&
            retVal.getDeclaringClass().isAssignableFrom(clazz)) {
            return retVal;
        }
        
        while (clazz != null) {
            if (clazz.getName().equals(declaringClass)) break;
            
            clazz = clazz.getSuperclass();
        }
        
        if (clazz == null) {
            return null;
        }
        
        try {
            retVal = clazz.getDeclaredField(name);
            soft = new SoftReference<Member>(retVal);
            
            return retVal;
        }
        catch (Throwable e) {
            return null;
        }
    }
    
    Method getMethod(final Class<?> clazz) {
        return AccessController.doPrivileged(new PrivilegedAction<Method>() {

            @Override
            public Method run() {
                return internalGetMethod(clazz);
            }
            
        });
    }
    
    private Method internalGetMethod(Class<?> clazz) {
        if (name == null || !isMethod) return null;
        Method retVal = (Method) soft.get();
        if (retVal == null) {
            this.classReflectionModel.cleanCache();
        }
        
        if (ClassReflectionModel.USE_SOFT_REFERENCE && (retVal != null) &&
            retVal.getDeclaringClass().isAssignableFrom(clazz)) {
            return retVal;
        }
        
        while (clazz != null) {
            if (clazz.getName().equals(declaringClass)) break;
            
            clazz = clazz.getSuperclass();
        }
        
        if (clazz == null) {
            return null;
        }
        
        ClassLoader loader = clazz.getClassLoader();
        
        Class<?> parameterTypes[] = new Class<?>[args.length];
        
        for (int lcv = 0; lcv < args.length; lcv++) {
            parameterTypes[lcv] = ClassReflectionModel.scalarClasses.get(args[lcv]);
            if (parameterTypes[lcv] != null) continue;
            
            if (args[lcv].startsWith("[")) {
                // Arrays must be handled with forName, not loadClass
                try {
                    parameterTypes[lcv] = Class.forName(args[lcv], false, loader);
                }
                catch (Throwable e) {
                    try {
                        parameterTypes[lcv] = Class.forName(args[lcv], false, ClassLoader.getSystemClassLoader());
                    }
                    catch (Throwable e2) {
                        return null;
                    }
                }
            }
            else {
                try {
                    parameterTypes[lcv] = loader.loadClass(args[lcv]);
                }
                catch (Throwable e) {
                    try {
                        parameterTypes[lcv] = ClassLoader.getSystemClassLoader().loadClass(args[lcv]);
                    }
                    catch (Throwable e2) {
                        return null;
                    }
                }
            }
        }
        
        try {
            retVal = clazz.getDeclaredMethod(name, parameterTypes);
            soft = new SoftReference<Member>(retVal);
            
            return retVal; 
        }
        catch (Throwable e) {
            return null;
        }
        
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("{");
        if (args != null) {
            boolean first = true;
            for (String arg : args) {
                if (first) {
                    sb.append(arg);
                    first = false;
                }
                else {
                    sb.append("," + arg);
                }
            }
        }
        sb.append("}");
        
        return "MethodDescriptor(" + ((isMethod) ? "method," : "field,") +
                name + "," +
                declaringClass + "," +
                sb.toString() + ")";
    }
}