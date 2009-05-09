/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.enterprise.configapi.tests.validation;

import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.configapi.tests.ConfigApiTest;
import java.beans.PropertyVetoException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hk2.config.ValidationException;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 */
@Ignore
public class JdbcConnectionPoolValidationTest extends ConfigApiTest {

    private JdbcConnectionPool pool = null;
    private static final String NAME = "test"; //same as the one in JdbcConnectionPoolValidation.xml

    public JdbcConnectionPoolValidationTest() {
    }

    @Override
    public String getFileName() {
        return ("JdbcConnectionPoolValidation");
    }

    @Before
    public void setUp() {
        pool = super.getHabitat().getComponent(JdbcConnectionPool.class, NAME);
    }

    @After
    public void tearDown() {
        pool = null;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test (expected=ValidationException.class)
    public void testBooleanDoesNotTakeInteger1() throws Throwable {
        try {
            ConfigSupport.apply(new SingleConfigCode<JdbcConnectionPool>() {
                public Object run(JdbcConnectionPool jdbcConnectionPool) throws PropertyVetoException, TransactionFailure {
                    jdbcConnectionPool.setConnectionLeakReclaim("123"); //this method should only take boolean;
                    return null;
                }
            }, pool);

        } catch(TransactionFailure e) {
            throw e.getCause().getCause();
        }
    }


    @Test
    public void testBooleanTakesTrueFalse() {
        try {
            pool.setSteadyPoolSize("true"); //this only takes a boolean
            pool.setSteadyPoolSize("false"); //this only takes a boolean
            pool.setSteadyPoolSize("TRUE"); //this only takes a boolean
            pool.setSteadyPoolSize("FALSE"); //this only takes a boolean
            pool.setSteadyPoolSize("FALSE"); //this only takes a boolean
        } catch(PropertyVetoException pv) {
            //ignore?
        }
    }
}