package org.glassfish.vmcluster.spi;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: 3/29/11
 * Time: 4:42 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MemoryListener {

    public void notified(VirtualMachine vm, long memory);
}
