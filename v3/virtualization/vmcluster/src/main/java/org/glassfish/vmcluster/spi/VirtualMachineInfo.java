package org.glassfish.vmcluster.spi;

import java.util.concurrent.TimeUnit;

/**
 * Returns the virtual machine information
 * @author Jerome Dochez
 */
public interface VirtualMachineInfo {

    /**
     * return the number of virtual CPUs allocated to the virtual machine
     *
     * @return number of virtual CPUs
     */
    int nbVirtCpu() throws VirtException;

    /**
     * returns the currently used memory
     *
     * @return the used memory
     */
    long memory() throws VirtException;

    /**
     * Returns the maximum memory allocated to this virtual machine.
     *
     * @return the virtual machine maximum memory.
     */
    long maxMemory() throws VirtException;

    /**
     * Returns the machine's state
     * @return the machine's state
     *
     * @throws VirtException if the machine's state cannot be obtained
     */
    Machine.State getState() throws VirtException;

    /**
     * Registers a memory changes listener
     * @param ml the memory listener instance
     * @param delay notification interval for memory changes polling.
     * @param unit the time unit to express delay
     */
    void registerMemoryListener(MemoryListener ml, long delay, TimeUnit unit);

    /**
     * Un-registers a memory changes listener
     * @param ml, the listener to un-register.
     */
    void unregisterMemoryListener(MemoryListener ml);
}
