
package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author Nandini Ektare
 */
public class SolarisRealmPropertyCheckValidator
    implements ConstraintValidator<SolarisRealmPropertyCheck, AuthRealm> {

    private static final String SOLARIS_REALM =
        "com.sun.enterprise.security.auth.realm.solaris.SolarisRealm";

    public void initialize(final SolarisRealmPropertyCheck fqcn) {
    }

    public boolean isValid(final AuthRealm realm,
        final ConstraintValidatorContext constraintValidatorContext) {

        if (realm.getClassname().equals(SOLARIS_REALM)) {
            Property jaas_context = realm.getProperty("jaas-context");
            if (jaas_context == null || jaas_context.getName().equals(""))
                return false;
        }
        
        return true;
    }
}





