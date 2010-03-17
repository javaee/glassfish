package org.glassfish.hk2.classmodel.reflect.test.model;

import org.glassfish.hk2.classmodel.reflect.test.model.qualifier.Synchronous;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 12, 2010
 * Time: 2:46:08 PM
 * To change this template use File | Settings | File Templates.
 */
@Synchronous
public class SynchronousPaymentProcessor implements PaymentProcessor {
    
    @Override
    public void process(Payment payment) {
        
    }
}
