package org.jvnet.hk2.config.test;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.Populator;

/**
* Created by IntelliJ IDEA.
* User: makannan
* Date: 5/2/12
* Time: 11:11 AM
* To change this template use File | Settings | File Templates.
*/
@Service
public class DummyPopulator
    implements Populator {

    private boolean populateCalled;

    public void run(ConfigParser p) {
        populateCalled = true;
    }

    public boolean isPopulateCalled() {
        return populateCalled;
    }
}
