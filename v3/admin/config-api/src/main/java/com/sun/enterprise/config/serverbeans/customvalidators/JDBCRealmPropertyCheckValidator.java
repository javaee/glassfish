
package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.config.serverbeans.AuthRealm;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.jvnet.hk2.config.types.Property;

/**
 *
 * @author Nandini Ektare
 */
public class JDBCRealmPropertyCheckValidator
    implements ConstraintValidator<JDBCRealmPropertyCheck, AuthRealm> {

    private static final String JDBC_REALM =
        "com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm";

    public void initialize(final JDBCRealmPropertyCheck fqcn) {
    }

    public boolean isValid(final AuthRealm realm,
        final ConstraintValidatorContext constraintValidatorContext) {

        if (realm.getClassname().equals(JDBC_REALM)) {
            Property jaas_context = realm.getProperty("jaas-context");
            Property ds_jndi = realm.getProperty("datasource-jndi");
            Property user_table = realm.getProperty("user-table");
            Property group_table = realm.getProperty("group-table");
            Property user_name_col = realm.getProperty("user-name-column");
            Property passwd_col = realm.getProperty("password-column");
            Property grp_name_col = realm.getProperty("group-name-column");
            Property digest_algo = realm.getProperty("digest-algorithm");

            if (jaas_context == null || jaas_context.getName().equals(""))
                return false;

            if (ds_jndi == null || ds_jndi.getName().equals(""))
                return false;

            if (user_table == null || user_table.getName().equals(""))
                return false;

            if (group_table == null || group_table.getName().equals(""))
                return false;

            if (user_name_col == null || user_name_col.getName().equals(""))
                return false;

            if (passwd_col == null || passwd_col.getName().equals(""))
                return false;

            if (grp_name_col == null || grp_name_col.getName().equals(""))
                return false;

            if (digest_algo == null)
                    return false;

            if ("none".equalsIgnoreCase(digest_algo.getName())) {
                try {
                    MessageDigest.getInstance(digest_algo.getName());
                } catch(NoSuchAlgorithmException e) {
                    return false;
                }
            }
        }

        return true;
    }
}





