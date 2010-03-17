// Copyright
package org.glassfish.hk2.classmodel.reflect.test.model;

import org.glassfish.hk2.classmodel.reflect.test.model.qualifier.Synchronous;

import javax.inject.Inject;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Feb 23, 2010
 * Time: 11:15:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class PaymentHandler {

    @Inject @Synchronous
    PaymentProcessor payment;

    
}
