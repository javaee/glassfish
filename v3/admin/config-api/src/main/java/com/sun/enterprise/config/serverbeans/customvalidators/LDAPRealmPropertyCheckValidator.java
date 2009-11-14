
package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author Nandini Ektare
 */
public class LDAPRealmPropertyCheckValidator
    implements ConstraintValidator<LDAPRealmPropertyCheck, AuthRealm> {

    private static final String LDAP_REALM =
        "com.sun.enterprise.security.auth.realm.ldap.LDAPRealm";

    public void initialize(final LDAPRealmPropertyCheck fqcn) {
    }

    public boolean isValid(final AuthRealm realm,
        final ConstraintValidatorContext constraintValidatorContext) {

        if (realm.getClassname().equals(LDAP_REALM)) {
            Property jaas_context = realm.getProperty("jaas-context");
            Property dn = realm.getProperty("base-dn");
            Property url = realm.getProperty("directory");

            if (jaas_context == null || jaas_context.getName().equals(""))
                return false;

            if (url == null || url.getName().equals(""))
                return false;

            if (dn == null || dn.getName().equals(""))
                return false;
        }
        
        return true;
    }
}





