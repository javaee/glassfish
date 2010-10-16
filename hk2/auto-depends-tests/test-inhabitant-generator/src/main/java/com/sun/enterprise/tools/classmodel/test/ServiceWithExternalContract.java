package com.sun.enterprise.tools.classmodel.test;

import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.tools.classmodel.test.external.ExternalContract;

/**
 * Used in testing of a service interface with a contract in another
 * jar. 
 *
 * @author Jeff Trent
 *
 */
@Service
public class ServiceWithExternalContract implements ExternalContract {

}
