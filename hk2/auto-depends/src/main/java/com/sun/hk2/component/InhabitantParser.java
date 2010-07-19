package com.sun.hk2.component;

import org.jvnet.hk2.component.MultiMap;

/**
 * Abstraction of inhabitant meta-data retrieval capability.
 *
 * @author Jerome Dochez
 */
public interface InhabitantParser {

    Iterable<String> getIndexes();

    String getImplName();

    void setImplName(String name);

    String getLine();

    void rewind();

    MultiMap<String, String> getMetaData();
}
