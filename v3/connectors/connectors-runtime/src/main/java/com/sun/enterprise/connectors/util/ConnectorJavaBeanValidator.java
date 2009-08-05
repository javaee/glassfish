/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.connectors.util;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.Singleton;
import org.jvnet.hk2.component.PostConstruct;

import javax.validation.*;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.bootstrap.GenericBootstrap;
import java.util.Set;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.io.InputStream;

import com.sun.logging.LogDomains;
import com.sun.enterprise.connectors.ConnectorRegistry;

@Service
public class ConnectorJavaBeanValidator {

    private final static Logger _logger = LogDomains.getLogger(
            ConnectorJavaBeanValidator.class, LogDomains.RSR_LOGGER);

    public boolean validateJavaBean(Object bean, String rarName) {
        if (bean != null) {
            Validator validator = ConnectorRegistry.getInstance().getBeanValidator(rarName);
            if (validator != null) {
                BeanDescriptor bd =
                        validator.getConstraintsForClass(bean.getClass());
                bd.getConstraintDescriptors();

                Class array[] = new Class[]{};
                Set constraintViolations = validator.validate(bean, array);

                if (constraintViolations != null) {
                    Iterator it = constraintViolations.iterator();
                    boolean violated = false;
                    String msg = "Constraints for this bean violated. \n Message = ";
                    while (it.hasNext()) {
                        violated = true;
                        ConstraintViolation cv = (ConstraintViolation) it.next();
                        msg = msg + cv.getPropertyPath() + " " + cv.getMessage();
                    }
                    if (violated) {
                        _logger.log(Level.SEVERE, "Following validation constraints violated " +
                                "for bean of type [ " + bean.getClass() + " ] : " + msg);
                        throw new ValidationException(msg);
                    }
                    return false;
                }
                return true;
            } else {
                _logger.log(Level.FINE, "no bean validator is available for RAR [ " + rarName + " ]");
                return true;
            }
        }
        throw new ValidationException("null Bean passed for validation");
    }
}