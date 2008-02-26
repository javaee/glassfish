package com.sun.hk2.component;

import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import sun.misc.BASE64Decoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Collection;
import java.util.Collections;

/**
 * Partial implementation of {@link Inhabitant} that defines methods whose
 * semantics is fixed by {@link Habitat}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractInhabitantImpl<T> implements Inhabitant<T>  {
    private Collection<Inhabitant> companions;

    public final T get() {
        return get(this);
    }

    public <T> T getSerializedMetadata(final Class<T> type, String key) {
        String v = metadata().getOne(key);
        if(v==null)     return null;

        try {
            ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(new BASE64Decoder().decodeBuffer(v))) {
                final ClassLoader cl = type.getClassLoader();

                /**
                 * Use ClassLoader of the given type. Otherwise by default we end up using the classloader
                 * that loaded HK2, which won't be able to see most of the user classes.
                 */
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    String name = desc.getName();
                    try {
                        return Class.forName(name,false,cl);
                    } catch (ClassNotFoundException ex) {
                        return super.resolveClass(desc);
                    }
                }
            };

            return type.cast(is.readObject());
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public final <T> T getSerializedMetadata(Class<T> type) {
        return getSerializedMetadata(type,type.getName());
    }

    public Inhabitant lead() {
        return null;
    }

    public final Collection<Inhabitant> companions() {
        if(companions==null)    return Collections.emptyList();
        else                    return companions;
    }

    public final void setCompanions(Collection<Inhabitant> companions) {
        this.companions = companions;
    }
}
