package org.jvnet.hk2.config.generator;

import org.jvnet.hk2.annotations.Contract;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Set;

/**
 * A Utility class to find Classes or Interfaces annotated with @Contract
 */
public class HK2ContractFinder {
    /**
     * The entry point.
     */
    public static Set<TypeElement> find(Types types, TypeElement d) {
        return new HK2ContractFinder(types).check(d).result;
    }


    private final Types types;
    /**
     * {@link TypeElement}s whose contracts are already checked.
     */
    private final Set<TypeElement> checkedInterfaces = new HashSet<TypeElement>();

    private final Set<TypeElement> result = new HashSet<TypeElement>();

    private HK2ContractFinder(Types types) {
        this.types = types;
    }

    private HK2ContractFinder check(TypeElement d) {

        // traverse up the inheritance tree and find all supertypes that have @Contract
        while(true) {
            checkSuperInterfaces(d);
            if (d.getKind() == ElementKind.CLASS) {
                checkContract(d);
                TypeMirror tm = d.getSuperclass();
                if (tm.getKind() == TypeKind.DECLARED) {
                    Element e = types.asElement(tm);
                    if (e instanceof TypeElement) {
                        d = (TypeElement) e;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            } else
                break;
        }

        return this;
    }

    private void checkContract(TypeElement type) {
        if (type.getAnnotation(Contract.class) != null)
            result.add(type);
    }

    private void checkSuperInterfaces(TypeElement d) {
        for (TypeMirror intf : d.getInterfaces()) {
            TypeElement i = (TypeElement) types.asElement(intf);
            if(checkedInterfaces.add(i)) {
                checkContract(i);
                checkSuperInterfaces(i);
            }
        }
    }
}
