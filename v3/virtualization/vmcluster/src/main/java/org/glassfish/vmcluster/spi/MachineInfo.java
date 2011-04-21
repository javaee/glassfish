package org.glassfish.vmcluster.spi;

/**
 * Returns static information about a machine like it's memory, number of processors, etc..
 * @author Jerome Dochez
 */
public interface MachineInfo {

    /**
     * Returns the memory size of this machine
     * @return the memory size
     */
    long memory();

    /**
     * Returns the number of processors of this machine
     * @return the cpu number
     */
    int cpus();

    /**
     * Returns the number of cores
     * @return the number of cores
     */
    int cores();
}
