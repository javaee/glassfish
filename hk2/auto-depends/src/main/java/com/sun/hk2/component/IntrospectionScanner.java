package com.sun.hk2.component;

import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.jvnet.hk2.annotations.Contract;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Sep 27, 2010
 * Time: 9:44:56 PM
 * To change this template use File | Settings | File Templates.
 */
@Contract
public interface IntrospectionScanner {

    void parse(ParsingContext context, Holder<ClassLoader> loader);
}
