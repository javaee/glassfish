package com.sun.hk2.component;

import org.glassfish.hk2.classmodel.reflect.Type;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.jvnet.hk2.annotations.Service;


/**
 * Use a class model to build a list of inhabitants.
 */
public class InhabitantsFromModel {


    /**
     * Parses the inhabitants file (which is represented by {@link InhabitantsScanner}.
     *
     * <p>
     * All the earlier drop/replace commands will be honored during this process.
     */
    public void parse(Types types, Holder<ClassLoader> classLoader){
        Type service = types.getByName(Service.class.getName());
        
    }
}
