package com.sun.enterprise.tools.classmodel.test;

import org.jvnet.hk2.annotations.Service;

/**
 * Used in testing of a service having an abstract base with an
 * @Contract interface in another jar. 
 *
 * @author Jeff Trent
 *
 */
@Service
public class ServiceWithAbstractBaseHavingExternalContract extends AbstractBase {

}
