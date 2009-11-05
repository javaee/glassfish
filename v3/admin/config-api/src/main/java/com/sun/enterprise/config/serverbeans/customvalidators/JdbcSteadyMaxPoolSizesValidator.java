
package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author Nandini Ektare
 */
public class JdbcSteadyMaxPoolSizesValidator
    implements ConstraintValidator<JdbcSteadyMaxPoolSizes, JdbcConnectionPool> {
    public void initialize(final JdbcSteadyMaxPoolSizes fqcn) {
    }

    public boolean isValid(final JdbcConnectionPool pool,
        final ConstraintValidatorContext constraintValidatorContext) {

        if (Integer.parseInt(pool.getMaxPoolSize()) <
            (Integer.parseInt(pool.getSteadyPoolSize())) ) {
            return false;
        }
        else return true;
    }
}





