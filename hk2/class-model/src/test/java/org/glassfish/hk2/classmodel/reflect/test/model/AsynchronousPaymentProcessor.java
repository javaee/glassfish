package org.glassfish.hk2.classmodel.reflect.test.model;

import org.glassfish.hk2.classmodel.reflect.test.model.qualifier.Asynchronous;

/**
 * Created by IntelliJ IDEA.
 * User: dochez
 * Date: Jan 12, 2010
 * Time: 2:46:22 PM
 * To change this template use File | Settings | File Templates.
 */
@Asynchronous
public class AsynchronousPaymentProcessor implements PaymentProcessor {

    @Override
    public void process(Payment payment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
