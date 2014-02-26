package org.glassfish.hk2.utilities.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

class MemberKey {
    /**
     * 
     */
    private final ClassReflectionModel classReflectionModel;
    private final MemberDescriptor backingMember;
    private final int hashCode;
    private final boolean postConstruct;
    private final boolean preDestroy;

    MemberKey(ClassReflectionModel classReflectionModel, Member method, boolean isPostConstruct, boolean isPreDestroy) {
        this.classReflectionModel = classReflectionModel;
        backingMember = new MemberDescriptor(this.classReflectionModel, method);
        hashCode = backingMember.hashCode();
        postConstruct = isPostConstruct;
        preDestroy = isPreDestroy;
    }
    
    Method getBackingMethod(Class<?> clazz) {
        return backingMember.getMethod(clazz);
    }
    
    Field getBackingField(Class<?> clazz) {
        return backingMember.getField(clazz);
    }

    boolean isPostConstruct() {
        return postConstruct;
    }

    boolean isPreDestroy() {
        return preDestroy;
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof MemberKey)) return false;

        MemberKey omk = (MemberKey) o;
        
        return (backingMember.equals(omk.backingMember));
    }
}