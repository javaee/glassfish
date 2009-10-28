
package com.sun.enterprise.config.serverbeans.customvalidators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Implementation for the user-defined constraint annotation @JavaClassName 
 * @author Nandini Ektare
 */
public class JavaClassNameValidator
implements ConstraintValidator<JavaClassName, String> {

    public void initialize(final JavaClassName fqcn) {}

    public boolean isValid(final String fullyQualifiedClassName,                
        final ConstraintValidatorContext constraintValidatorContext) {

        try {
            return isValidPackageName(fullyQualifiedClassName);
        } catch (Exception e) {
            return false;
        }
    }

    /** Is the given string a valid package name? */
    private boolean isValidPackageName(String fqcn) {
        int index;

        if (fqcn.indexOf('.') == -1) {
            return isValidClassName(fqcn);
        }

        while ((index = fqcn.indexOf('.')) != -1) {
            if (!isValidClassName(fqcn.substring(0, index))) {
                return false;
            }
            fqcn = fqcn.substring(index+1);
        }
        return isValidClassName(fqcn);
    }

    private boolean isValidClassName(String className) {
        boolean valid = true;
        for(int i=0;i<className.length();i++) {
            if(i == 0) {
                if(!Character.isJavaIdentifierStart(className.charAt(i)))
                    valid = false;
            }
            if(!Character.isJavaIdentifierPart(className.charAt(i)))
                valid = false;
        }
        return valid;
    }
}