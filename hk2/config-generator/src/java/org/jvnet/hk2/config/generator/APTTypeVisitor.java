package org.jvnet.hk2.config.generator;

import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;

public abstract class APTTypeVisitor<T,P> {
    public final T apply(TypeMirror type, P param) {
        if( type instanceof ArrayType)
            return onArrayType((ArrayType)type,param);
        if( type instanceof PrimitiveType)
            return onPrimitiveType((PrimitiveType)type,param);
        if (type instanceof ClassType)
            return onClassType((ClassType)type,param);
        if (type instanceof InterfaceType)
            return onInterfaceType((InterfaceType)type,param);
        if (type instanceof TypeVariable)
            return onTypeVariable((TypeVariable)type,param);
        if (type instanceof VoidType)
            return onVoidType((VoidType)type,param);
        if(type instanceof WildcardType)
            return onWildcard((WildcardType) type,param);
        assert false;
        throw new IllegalArgumentException();
    }

    protected abstract T onPrimitiveType(PrimitiveType type, P param);
    protected abstract T onArrayType(ArrayType type, P param);
    protected abstract T onClassType(ClassType type, P param);
    protected abstract T onInterfaceType(InterfaceType type, P param);
    protected abstract T onTypeVariable(TypeVariable type, P param);
    protected abstract T onVoidType(VoidType type, P param);
    protected abstract T onWildcard(WildcardType type, P param);

}