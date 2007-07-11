package org.jvnet.hk2.config;

import org.jvnet.hk2.annotations.Contract;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Kohsuke Kawaguchi
 */
@Contract
public abstract class Converter<T> {
    public final Class<T> type;

    protected Converter(Class<T> type) {
        this.type = type;
    }

    public abstract T read(String value);

    private static final Map<Class,Converter> BUILTIN_CONVERTERS = new HashMap<Class,Converter>();

    static {
        for( Converter t : new Converter[]{
            new Converter<Boolean>(Boolean.class) {
                public Boolean read(String value) {
                    return Boolean.getBoolean(value);
                }
            },
            new Converter<Integer>(Integer.class) {
                public Integer read(String value) {
                    return Integer.parseInt(value);
                }
            }
        }) {
            BUILTIN_CONVERTERS.put(t.type,t);
        }
    }
}
