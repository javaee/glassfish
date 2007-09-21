package com.sun.enterprise.tools.apt;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.MirroredTypeException;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractProvided;

import java.util.HashSet;
import java.util.Set;

/**
 * Given a {@link ClassDeclaration},
 * find all super-types that have {@link Contract},
 * including ones pointed by {@link ContractProvided}.
 *
 * @author Kohsuke Kawaguchi
 */
public class ContractFinder {
    /**
     * The entry point.
     */
    public static Set<TypeDeclaration> find(ClassDeclaration d) {
        return new ContractFinder().check(d).result;
    }


    /**
     * {@link InterfaceType}s whose contracts are already checked.
     */
    private final Set<InterfaceDeclaration> checkedInterfaces = new HashSet<InterfaceDeclaration>();

    private final Set<TypeDeclaration> result = new HashSet<TypeDeclaration>();

    private ContractFinder() {
    }

    private ContractFinder check(ClassDeclaration d) {
        // look for @ContractProvided interface
        checkContractProvided(d);

        // traverse up the inheritance tree and find all supertypes that have @Contract
        ClassDeclaration sd = d;
        while(sd.getSuperclass()!=null) {
            checkSuperInterfaces(d);

            sd = sd.getSuperclass().getDeclaration();
            checkContract(sd);
        }

        return this;
    }

    private void checkContractProvided(ClassDeclaration impl) {
        ContractProvided provided = impl.getAnnotation(ContractProvided.class);
        if (provided != null) {
            try {
                provided.value();
            } catch (MirroredTypeException e) {
                result.add(((DeclaredType)e.getTypeMirror()).getDeclaration());
            }
        }
    }

    private void checkContract(TypeDeclaration type) {
        if (type.getAnnotation(Contract.class) != null)
            result.add(type);
    }

    private void checkSuperInterfaces(TypeDeclaration d) {
        for (InterfaceType intf : d.getSuperinterfaces()) {
            InterfaceDeclaration i = intf.getDeclaration();
            if(checkedInterfaces.add(i)) {
                checkContract(i);
                checkSuperInterfaces(i);
            }
        }
    }
}
