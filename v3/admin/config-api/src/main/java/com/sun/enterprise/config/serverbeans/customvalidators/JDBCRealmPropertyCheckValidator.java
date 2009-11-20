
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
    private static final String DEFAULT_DIGEST_ALGORITHM = "MD5";

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

            if ((jaas_context == null) || (ds_jndi == null) ||
                (user_table == null) || (group_table == null) ||
                (user_name_col == null) || (passwd_col == null) ||
                (grp_name_col == null)) {
                
                return false;
            }
            
            if (digest_algo != null) {
                String algoName = digest_algo.getValue();

                if (!("none".equalsIgnoreCase(algoName))) {
                    try {
                        MessageDigest.getInstance(algoName);
                    } catch(NoSuchAlgorithmException e) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}





