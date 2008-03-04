package org.glassfish.admin.runtime.infrastructure.management;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Object encapsulating the same information as a Method but that we can
 * instantiate explicitly.
 */
public class XMethod {
    public XMethod(Method m) {
        this(m.getName(), m.getParameterTypes(), m.getReturnType());
    }

    public XMethod(String name, Class<?>[] paramTypes, Class<?> returnType) {
        this.name = name;
        this.paramTypes = paramTypes;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public Class<?>[] getParameterTypes() {
        return paramTypes.clone();
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object x) {
        if (!(x instanceof XMethod))
            return false;
        XMethod xm = (XMethod) x;
        return (name.equals(xm.name) && returnType.equals(xm.returnType) &&
                Arrays.equals(paramTypes, xm.paramTypes));
    }

    @Override
    public int hashCode() {
        return name.hashCode() + returnType.hashCode() +
                Arrays.hashCode(paramTypes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(returnType.getName()).append(" ").append(name).append("(");
        String comma = "";
        for (Class<?> paramType : paramTypes) {
            sb.append(comma).append(paramType.getName());
            comma = ", ";
        }
        sb.append(")");
        return sb.toString();
    }

    private final String name;
    private final Class<?>[] paramTypes;
    private final Class<?> returnType;
}
