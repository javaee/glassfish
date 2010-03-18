package com.sun.hk2.component;

import org.jvnet.hk2.annotations.Contract;

/**
 * Decorator for the InhabitantParser
 *
 * @author Jerome Dochez
 */
@Contract
public interface InhabitantsParserDecorator {

    /**
     * decorator names, there can be multiple decorators defined, each for a
     * particular usage.
     * @return the decorator name
     */
    public String getName();

    /**
     * Decorate the passed instance
     * @param parser instance to decorate
     */
    public void decorate(InhabitantsParser parser);
}
