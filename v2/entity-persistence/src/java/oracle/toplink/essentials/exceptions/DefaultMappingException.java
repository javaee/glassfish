/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.exceptions;

import java.util.List;
import oracle.toplink.essentials.exceptions.i18n.ExceptionMessageGenerator;

/**
 * <P><B>Purpose</B>:
 * Wrapper for any exception that occurred through OC4J cmp deafult mapping.
 */
public class DefaultMappingException extends TopLinkException {
    public final static int FINDER_PARAMETER_TYPE_NOT_FOUND = 20001;
    public final static int FINDER_NOT_DEFINED_IN_HOME = 20002;
    public final static int EJB_SELECT_NOT_DEFINED_IN_BEAN = 20003;
    public final static int FINDER_NOT_START_WITH_FIND_OR_EJBSELECT = 20004;
    public final static int GETTER_NOT_FOUND = 20005;
    public final static int FIELD_NOT_FOUND = 20006;

    public DefaultMappingException(String message) {
        super(message);
    }

    protected DefaultMappingException(String message, Exception internalException) {
        super(message, internalException);
    }

    public static DefaultMappingException finderParameterTypeNotFound(String beanName, String finderName, String finderParameterTypeString) {
        Object[] args = { beanName, finderName, finderParameterTypeString };

        DefaultMappingException exception = new DefaultMappingException(ExceptionMessageGenerator.buildMessage(DefaultMappingException.class, FINDER_PARAMETER_TYPE_NOT_FOUND, args));
        exception.setErrorCode(FINDER_PARAMETER_TYPE_NOT_FOUND);
        return exception;
    }

    public static DefaultMappingException finderNotDefinedInHome(String beanName, String finderName, List finderParameters) {
        Object[] args = { beanName, finderName, finderParameters.toArray() };
        DefaultMappingException exception = new DefaultMappingException(ExceptionMessageGenerator.buildMessage(DefaultMappingException.class, FINDER_NOT_DEFINED_IN_HOME, args));
        exception.setErrorCode(FINDER_NOT_DEFINED_IN_HOME);
        return exception;
    }

    public static DefaultMappingException finderNotStartWithFindOrEjbSelect(String beanName, String finderName) {
        Object[] args = { beanName, finderName };
        DefaultMappingException exception = new DefaultMappingException(ExceptionMessageGenerator.buildMessage(DefaultMappingException.class, FINDER_NOT_START_WITH_FIND_OR_EJBSELECT, args));
        exception.setErrorCode(FINDER_NOT_START_WITH_FIND_OR_EJBSELECT);
        return exception;
    }

    public static DefaultMappingException ejbSelectNotDefinedInBean(String beanName, String ejbSelectName, List ejbSelectParameters) {
        Object[] args = { beanName, ejbSelectName, ejbSelectParameters.toArray() };
        DefaultMappingException exception = new DefaultMappingException(ExceptionMessageGenerator.buildMessage(DefaultMappingException.class, EJB_SELECT_NOT_DEFINED_IN_BEAN, args));
        exception.setErrorCode(EJB_SELECT_NOT_DEFINED_IN_BEAN);
        return exception;
    }

    public static DefaultMappingException getterNotFound(String getter, String beanName) {
        Object[] args = { getter, beanName };
        DefaultMappingException exception = new DefaultMappingException(ExceptionMessageGenerator.buildMessage(DefaultMappingException.class, GETTER_NOT_FOUND, args));
        exception.setErrorCode(GETTER_NOT_FOUND);
        return exception;
    }

    public static DefaultMappingException fieldNotFound(String field, String beanName) {
        Object[] args = { field, beanName };
        DefaultMappingException exception = new DefaultMappingException(ExceptionMessageGenerator.buildMessage(DefaultMappingException.class, FIELD_NOT_FOUND, args));
        exception.setErrorCode(FIELD_NOT_FOUND);
        return exception;
    }
}
