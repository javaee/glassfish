package org.jvnet.hk2.test.multiple.noindex;

import org.jvnet.hk2.annotations.Service;

/**
 * Child and Parent interfaces implementation
 * 
 * @author Jerome Dochez
 */
@Service
public class ChildImpl implements ChildIntf {

    @Override
    public String child() {
        return "child";
    }

    @Override
    public String parent() {
        return "parent";
    }
}
