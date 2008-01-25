package com.sun.enterprise.tools.apt;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;

import java.util.HashSet;
import java.util.Set;

/**
 * Recursively decent all the super classes and super interfaces.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class TypeHierarchyVisitor<P> {
    /**
     * {@link InterfaceType}s whose contracts are already checked.
     */
    protected final Set<InterfaceDeclaration> visited = new HashSet<InterfaceDeclaration>();

    protected void check(TypeDeclaration d, P param) {
        if (d instanceof ClassDeclaration)
            checkClass((ClassDeclaration) d,param);
        else
            checkInterface((InterfaceDeclaration)d,param);
    }

    protected void checkInterface(InterfaceDeclaration id, P param) {
        checkSuperInterfaces(id,param);
    }

    protected void checkClass(ClassDeclaration cd, P param) {
        ClassType sc = cd.getSuperclass();
        if(sc!=null)
            check(sc.getDeclaration(),param);

        checkSuperInterfaces(cd,param);
    }

    protected void checkSuperInterfaces(TypeDeclaration d, P param) {
        for (InterfaceType intf : d.getSuperinterfaces()) {
            InterfaceDeclaration i = intf.getDeclaration();
            if(visited.add(i)) {
                check(i,param);
            }
        }
    }
}
