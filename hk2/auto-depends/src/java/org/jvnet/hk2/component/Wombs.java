package org.jvnet.hk2.component;

import com.sun.hk2.component.ConstructorWomb;
import com.sun.hk2.component.FactoryWomb;
import org.jvnet.hk2.annotations.Factory;

/**
 * @author Kohsuke Kawaguchi
 */
public class Wombs {
    public static <T> Womb<T> create(Class<T> c, Habitat habitat, MultiMap<String,String> metadata) {
        Factory f = c.getAnnotation(Factory.class);

        if(f==null)
            return new ConstructorWomb<T>(c,habitat,metadata);
        else
            return new FactoryWomb<T>(c,f.value(),habitat,metadata);
    }
}
