package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.ScheduledTimerDescriptor;

import org.jvnet.hk2.annotations.Contract;

/**
 * ScheduledTimerValidator contract, needed until we move EJB descriptors to
 * relevant containers.
 *
 * @author Marina Vatkina
 * 
 */
@Contract
public interface ScheduledTimerValidator {

    /**
     * Validate provided ScheduledTimerDescriptor.
     */
    public void validateScheduledTimerDescriptor(ScheduledTimerDescriptor sd);
}
