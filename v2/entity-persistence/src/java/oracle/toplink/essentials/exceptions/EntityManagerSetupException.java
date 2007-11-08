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

import oracle.toplink.essentials.exceptions.i18n.*;

public class EntityManagerSetupException extends TopLinkException {
    public static final int SESSIONS_XML_VALIDATION_EXCEPTION = 28001;
    public static final int WRONG_SESSION_TYPE_EXCEPTION = 28002;
    public static final int MISSING_SERVER_PLATFORM_EXCEPTION = 28003;
    public static final int ERROR_IN_SETUP_OF_EM = 28004;
    public static final int EXCEPTION_IN_SETUP_OF_EM = 28005;
    public static final int CLASS_NOT_FOUND_FOR_PROPERTY = 28006;
    public static final int FAILED_TO_INSTANTIATE_SERVER_PLATFORM = 28007;
    public static final int CLASS_NOT_FOUND_WHILE_PROCESSING_ANNOTATIONS = 28008;
    public static final int ATTEMPTED_REDEPLOY_WITHOUT_CLOSE = 28009;
    public static final int JTA_PERSISTENCE_UNIT_INFO_MISSING_JTA_DATA_SOURCE = 28010;
    public static final int SESSION_REMOVED_DURING_DEPLOYMENT = 28011;
    public static final int WRONG_PROPERTY_VALUE_TYPE = 28012;
    public static final int CANNOT_DEPLOY_WITHOUT_PREDEPLOY = 28013;
    public static final int FAILED_WHILE_PROCESSING_PROPERTY = 28014;
    public static final int FAILED_TO_INSTANTIATE_LOGGER = 28015;
    public static final int PU_NOT_EXIST = 28016;
    public static final int CANNOT_PREDEPLOY = 28017;
    public static final int PREDEPLOY_FAILED = 28018;
    public static final int DEPLOY_FAILED = 28019;
    public static final int WRONG_WEAVING_PROPERTY_VALUE = 28020;
    

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    public EntityManagerSetupException() {
        super();
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected EntityManagerSetupException(String message) {
        super(message);
    }

    /**
     * INTERNAL:
     * TopLink exceptions should only be thrown by TopLink.
     */
    protected EntityManagerSetupException(String message, Throwable internalException) {
        super(message);
        setInternalException(internalException);
    }

    public static EntityManagerSetupException attemptedRedeployWithoutClose(String sessionName) {
        Object[] args = { sessionName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, ATTEMPTED_REDEPLOY_WITHOUT_CLOSE, args));
        setupException.setErrorCode(ATTEMPTED_REDEPLOY_WITHOUT_CLOSE);
        return setupException;
    }

    public static EntityManagerSetupException missingServerPlatformException(String sessionName, String xmlFileName) {
        Object[] args = { sessionName, xmlFileName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, MISSING_SERVER_PLATFORM_EXCEPTION, args));
        setupException.setErrorCode(MISSING_SERVER_PLATFORM_EXCEPTION);
        return setupException;
    }

    public static EntityManagerSetupException sessionRemovedDuringDeployment(String sessionName){
        Object[] args = { sessionName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, SESSION_REMOVED_DURING_DEPLOYMENT, args));
        setupException.setErrorCode(SESSION_REMOVED_DURING_DEPLOYMENT);
        return setupException;	
    }
    
    public static EntityManagerSetupException sessionXMLValidationException(String sessionName, String xmlFileName, ValidationException exception) {
        Object[] args = { sessionName, xmlFileName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, SESSIONS_XML_VALIDATION_EXCEPTION, args), exception);
        setupException.setErrorCode(SESSIONS_XML_VALIDATION_EXCEPTION);
        return setupException;
    }

    public static EntityManagerSetupException wrongSessionTypeException(String sessionName, String xmlFileName, Exception exception) {
        Object[] args = { sessionName, xmlFileName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, WRONG_SESSION_TYPE_EXCEPTION, args), exception);
        setupException.setErrorCode(WRONG_SESSION_TYPE_EXCEPTION);
        return setupException;
    }

    public static EntityManagerSetupException errorInSetupOfEM() {
        Object[] args = {  };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, ERROR_IN_SETUP_OF_EM, args));
        setupException.setErrorCode(ERROR_IN_SETUP_OF_EM);
        return setupException;
    }

    public static EntityManagerSetupException exceptionInSetupOfEM(Exception exception) {
        Object[] args = {  };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, EXCEPTION_IN_SETUP_OF_EM, args), exception);
        setupException.setErrorCode(EXCEPTION_IN_SETUP_OF_EM);
        return setupException;
    }

    public static EntityManagerSetupException classNotFoundForProperty(String className, String propertyName, Exception exception) {
        Object[] args = { className, propertyName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, CLASS_NOT_FOUND_FOR_PROPERTY, args), exception);
        setupException.setErrorCode(CLASS_NOT_FOUND_FOR_PROPERTY);
        return setupException;
    }

    public static EntityManagerSetupException failedToInstantiateServerPlatform(String serverPlatformClass, String serverPlatformString, Exception exception) {
        Object[] args = { serverPlatformClass, serverPlatformString };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, FAILED_TO_INSTANTIATE_SERVER_PLATFORM, args), exception);
        setupException.setErrorCode(FAILED_TO_INSTANTIATE_SERVER_PLATFORM);
        return setupException;
    }
    
    public static EntityManagerSetupException classNotFoundWhileProcessingAnnotations(String className, Exception exception) {
        Object[] args = { className };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, CLASS_NOT_FOUND_WHILE_PROCESSING_ANNOTATIONS, args), exception);
        setupException.setErrorCode(CLASS_NOT_FOUND_WHILE_PROCESSING_ANNOTATIONS);
        return setupException;
    }
    
    public static EntityManagerSetupException jtaPersistenceUnitInfoMissingJtaDataSource(String persistenceUnitInfoName) {
        Object[] args = { persistenceUnitInfoName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, JTA_PERSISTENCE_UNIT_INFO_MISSING_JTA_DATA_SOURCE, args));
        setupException.setErrorCode(JTA_PERSISTENCE_UNIT_INFO_MISSING_JTA_DATA_SOURCE);
        return setupException;
    }
    
    public static EntityManagerSetupException wrongPropertyValueType(String value, String expectedType, String propertyName) {
        Object[] args = { value, expectedType, propertyName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, WRONG_PROPERTY_VALUE_TYPE, args));
        setupException.setErrorCode(WRONG_PROPERTY_VALUE_TYPE);
        return setupException;
    }
    
    public static EntityManagerSetupException cannotDeployWithoutPredeploy(String persistenceUnitName, String state) {
        Object[] args = { persistenceUnitName, state };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, CANNOT_DEPLOY_WITHOUT_PREDEPLOY, args));
        setupException.setErrorCode(CANNOT_DEPLOY_WITHOUT_PREDEPLOY);
        return setupException;
    }

    public static EntityManagerSetupException failedWhileProcessingProperty(String propertyName, String propertyValue, Exception exception) {
        Object[] args = { propertyName, propertyValue };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, FAILED_WHILE_PROCESSING_PROPERTY, args), exception);
        setupException.setErrorCode(FAILED_WHILE_PROCESSING_PROPERTY);
        return setupException;
    }

    public static EntityManagerSetupException failedToInstantiateLogger(String loggerClassName, String propertyName, Exception exception) {
        Object[] args = { loggerClassName, propertyName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, FAILED_TO_INSTANTIATE_LOGGER, args), exception);
        setupException.setErrorCode(FAILED_TO_INSTANTIATE_LOGGER);
        return setupException;
    }

    public static EntityManagerSetupException puNotExist(String puName) {
        Object[] args = { puName};

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, PU_NOT_EXIST, args));
        setupException.setErrorCode(PU_NOT_EXIST);
        return setupException;
    }

    public static EntityManagerSetupException cannotPredeploy(String persistenceUnitName, String state) {
        Object[] args = { persistenceUnitName, state };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, CANNOT_PREDEPLOY, args));
        setupException.setErrorCode(CANNOT_PREDEPLOY);
        return setupException;
    }

    public static EntityManagerSetupException predeployFailed(String persistenceUnitName, RuntimeException exception) {
        Object[] args = { persistenceUnitName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, PREDEPLOY_FAILED, args), exception);
        setupException.setErrorCode(PREDEPLOY_FAILED);
        return setupException;
    }

    public static EntityManagerSetupException deployFailed(String persistenceUnitName, RuntimeException exception) {
        Object[] args = { persistenceUnitName };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, DEPLOY_FAILED, args), exception);
        setupException.setErrorCode(DEPLOY_FAILED);
        return setupException;
    }
    
    public static EntityManagerSetupException wrongWeavingPropertyValue() {
        Object[] args = { };

        EntityManagerSetupException setupException = new EntityManagerSetupException(ExceptionMessageGenerator.buildMessage(EntityManagerSetupException.class, WRONG_WEAVING_PROPERTY_VALUE, args));
        setupException.setErrorCode(WRONG_WEAVING_PROPERTY_VALUE);
        return setupException;
    }
    
}
