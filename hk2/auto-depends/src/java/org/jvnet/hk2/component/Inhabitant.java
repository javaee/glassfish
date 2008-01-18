package org.jvnet.hk2.component;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.ScopeInstance;
import org.jvnet.hk2.annotations.Service;

import java.util.Collection;

/**
 * Represents a component in the world of {@link Habitat}.
 *
 * <p>
 * {@link Inhabitant} extends from {@link Holder}, as one of its
 * purposes is to encapsulate how we obtain an instance of a component.
 * On topf of that, {@link Inhabitant} enhances {@link Holder} by
 * adding more metadata that {@link Habitat} uses for finding
 * components and hooking them up together.
 *
 * <p>
 * All the methods exept {@link #get()} are immutable, meaning
 * they never change the value they return.
 *
 * @author Kohsuke Kawaguchi
 * @see Inhabitant
 */
public interface Inhabitant<T> extends Holder<T> {
    /**
     * The short-cut for {@code type().getName()}
     * but this allows us to defer loading the actual types.
     */
    String typeName();

    /**
     * Type of the inhabitant.
     *
     * <p>
     * The only binding contract that needs to be honored is that the {@link #get()}
     * method returns an instance assignable to this type. That is,
     * {@code get().getClass()==type()} doesn't necessarily have to hold,
     * but {@code type().isInstance(get())} must.
     *
     * <p>
     * This is particularly true when {@link Factory} is involved, as in such
     * case HK2 has no way of knowing the actual type.
     *
     * That said, this method is not designed for the semantics of
     * contract/implementation split --- implementations of a contract
     * should return the concrete type from this method, and use
     * {@link Habitat#addIndex(Inhabitant, String, String) habitat index}
     * to support look-up by contract. 
     *
     * @return
     *      Always non-null, same value.
     */
    Class<T> type();

    /**
     * Returns the instance of this inhabitant.
     *
     * <p>
     * Some {@link Inhabitant}s return the same instance for multiple
     * invocations (AKA singleton), but
     * the method may return different instances to invocations from different
     * context (AKA scope.) The extreme case is where the each invocation
     * returns a different object.
     */
    T get();
    
    T get(Inhabitant onBehalfOf);

    /**
     * Gets the metadata associated with this inhabitant.
     *
     * <p>
     * This data is usually used by a sub-system of HK2, and not really meant to
     * be used by applications. (At least for now.)
     * The main benefit of metadata is that it's available right away
     * as soon as the {@link Habitat} is properly initialized, even before
     * component classes are loaded. In contrast, accessing annotations would require
     * classes to be loaded and resolved.
     *
     * @return
     *      can be empty but never null. The values are read-only.
     * @see Service#metadata() 
     */
    MultiMap<String,String> metadata();

    /**
     * Called to orderly shutdown {@link Habitat}.
     * <p>
     * The expected behavior is for objects to get its {@link PreDestroy}
     * callback invoked, and its reference released. For singleton
     * objects, this method is expected to dispose that object.
     * <p>
     * For scoped objects, those are released when {@link ScopeInstance#release()}
     * is invoked.
     */
    void release();

//
// methods below here are more or less used for book-keeping purpose by Habitat,
// and implementations of Inhabitat should implement them just by using
// AbstractInhabitantImpl
//

    /**
     * If this inhabitant is a companion to another inhabitant (called "lead"),
     * This method returns that inhabitant. Otherwise null.
     */
    Inhabitant lead();

    /**
     * Returns the companion inhabitants associated with this inhabitant.
     *
     * <p>
     * This method works with the {@link #lead()} method in pairs, such
     * that the following condition always holds:
     *
     * <pre>x.companions().contains(y) &lt;-> y.lead()==x</pre>
     *
     * @return
     *      Can be empty but never null.
     */
    Collection<Inhabitant> companions();

    /**
     * This method is only meant to be invoked by {@link Habitat}.
     */
    void setCompanions(Collection<Inhabitant> companions);
}
