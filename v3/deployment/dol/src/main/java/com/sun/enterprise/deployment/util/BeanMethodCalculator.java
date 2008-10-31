package com.sun.enterprise.deployment.util;

import com.sun.enterprise.deployment.EjbDescriptor;

import java.util.Collection;
import java.util.Vector;

/**
 * BeanMethodCalculator contract, needed until we move EJB descriptors to
 * relevant containers.
 *
 * @author Jerome Dochez
 * 
 */
public interface BeanMethodCalculator {

    public Vector getPossibleCmpCmrFields(ClassLoader cl,
                                                 String className)
            throws ClassNotFoundException;

    public Vector getMethodsFor(EjbDescriptor ejbDescriptor, ClassLoader classLoader) 
            throws ClassNotFoundException;


    /**
     * @return a collection of MethodDescriptor for all the methods of my
     * ejb which are elligible to have a particular transaction setting.
     */
    public Collection getTransactionalMethodsFor(EjbDescriptor ejbDescriptor, ClassLoader loader)
        throws ClassNotFoundException, NoSuchMethodException;
    
}