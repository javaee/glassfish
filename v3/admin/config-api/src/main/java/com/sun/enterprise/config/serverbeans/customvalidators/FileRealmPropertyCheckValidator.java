
package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author Nandini Ektare
 */
public class FileRealmPropertyCheckValidator
    implements ConstraintValidator<FileRealmPropertyCheck, AuthRealm> {

    private static final String FILE_REALM =
        "com.sun.enterprise.security.auth.realm.file.FileRealm";

    public void initialize(final FileRealmPropertyCheck fqcn) {
    }

    public boolean isValid(final AuthRealm realm,
        final ConstraintValidatorContext constraintValidatorContext) {

        if (realm.getClassname().equals(FILE_REALM)) {
            Property jaas_context = realm.getProperty("jaas-context");
            Property file = realm.getProperty("file");

            if (jaas_context == null || jaas_context.getName().equals(""))
                return false;

            if (file == null || file.getName().equals(""))
                return false;
        }
        
        return true;
    }
}





