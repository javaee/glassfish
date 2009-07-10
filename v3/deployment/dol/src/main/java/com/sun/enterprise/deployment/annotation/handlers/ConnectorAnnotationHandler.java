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
package com.sun.enterprise.deployment.annotation.handlers;

import com.sun.enterprise.deployment.annotation.context.RarBundleContext;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.deployment.LicenseDescriptor;
import com.sun.enterprise.deployment.AuthMechanism;
import com.sun.enterprise.deployment.OutboundResourceAdapter;
import com.sun.enterprise.util.LocalStringManagerImpl;

import javax.resource.spi.Connector;
import javax.resource.spi.SecurityPermission;
import javax.resource.spi.AuthenticationMechanism;
import javax.resource.spi.security.GenericCredential;
import javax.resource.spi.security.PasswordCredential;
import javax.resource.spi.work.WorkContext;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.ietf.jgss.GSSCredential;
import org.glassfish.apf.*;
import org.glassfish.apf.impl.AnnotationUtils;
import org.glassfish.apf.impl.HandlerProcessingResultImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jagadish Ramu
 */
@Service
public class ConnectorAnnotationHandler extends AbstractHandler  {

    protected final static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(AbstractHandler.class);

    public Class<? extends Annotation> getAnnotationType() {
        return Connector.class;
    }

    public HandlerProcessingResult processAnnotation(AnnotationInfo element) throws AnnotationProcessorException {
        AnnotatedElementHandler aeHandler = element.getProcessingContext().getHandler();
        Connector connector = (Connector) element.getAnnotation();

        //TODO V3
        // need to check whether there are multiple @Connector annotations
        // decide which can be used (based on r-a-class in the descriptor
        // TODO V3
        // what if there isn't r-a-class specified in descriptor
        // current processing way is that, process the annotation, *directly* update the descriptor
        // this will cause issues when multiple annotations are present.

        // need a mechanism by which all @Connector annotations are found upfront,
        // decide which one is valid and use it / or throw exception when unable to decide
        // Above behavior will avoid inconsistent updates (mixed) from multiple @Connector annotations
        // i.e., first @Connector annotation is not the RA-Class as specified in descriptor and has a set of supported inflow context
        // second @Connector annotation is the class that is RA-Class of DD and does not define supported inflow-contexts 

        if (aeHandler instanceof RarBundleContext) {
            RarBundleContext rarContext = (RarBundleContext) aeHandler;
            ConnectorDescriptor desc = rarContext.getDescriptor();
            //TODO V3 don't use deprecated methods

            //TODO V3 For *all annotations* need to ignore "default" or unspecified attributes
            //TODO V3 make sure that the annotation defined defaults are the defaults of DD and DOL


            if (desc.getDescription().equals("") && connector.description().length > 0) {
                desc.setDescription(convertStringArrayToStringBuffer(connector.description()));
            }

            if (desc.getDisplayName().equals("") && connector.displayName().length > 0) {
                desc.setDisplayName(convertStringArrayToStringBuffer(connector.displayName()));
            }

            if ((desc.getSmallIconUri() == null || desc.getSmallIconUri().equals(""))
                    && connector.smallIcon().length > 0) {
                desc.setSmallIconUri(convertStringArrayToStringBuffer(connector.smallIcon()));
            }

            if ((desc.getLargeIconUri() == null || desc.getLargeIconUri().equals(""))
                    && connector.largeIcon().length > 0) {
                desc.setLargeIconUri(convertStringArrayToStringBuffer(connector.largeIcon()));
            }

            if (desc.getVendorName().equals("") && !connector.vendorName().equals("")) {
                desc.setVendorName(connector.vendorName());
            }

            if (desc.getEisType().equals("") && !connector.eisType().equals("")) {
                desc.setEisType(connector.eisType());
            }

            if (desc.getVersion().equals("") && !connector.version().equals("")) {
                desc.setVersion(connector.version());
            }

            if (desc.getLicenseDescriptor() == null) {
                //TODO V3 We will be able to detect whether license description is specified in annotation
                // or not, but "license required" can't be detected. Hence taking the annotated values *always*
                // if DD does not have an equivalent
                String[] licenseDescriptor = connector.licenseDescription();
                boolean licenseRequired = connector.licenseRequired();
                LicenseDescriptor ld = new LicenseDescriptor();
                ld.setDescription(convertStringArrayToStringBuffer(licenseDescriptor));
                ld.setLicenseRequired(licenseRequired);
                desc.setLicenseDescriptor(ld);
            }

            OutboundResourceAdapter ora = getOutbound(desc);

            //if (desc.getAuthMechanisms().size() == 0) {
            AuthenticationMechanism[] auths = connector.authMechanisms();
            if (auths != null && auths.length > 0) {
                for (AuthenticationMechanism auth : auths) {
                    String authMechString = auth.authMechanism();
                    int authMechInt = AuthMechanism.getAuthMechInt(authMechString);

                    // check whether the same auth-mechanism is defined in DD also,
                    // possible change could be with auth-mechanism's credential-interface for a particular
                    // auth-mechanism-type
                    boolean ignore = false;
                    Set ddAuthMechanisms = ora.getAuthMechanisms();

                    for (Object o : ddAuthMechanisms) {
                        AuthMechanism ddAuthMechanism = (AuthMechanism) o;
                        if (ddAuthMechanism.getAuthMechType().equals(auth.authMechanism())) {
                            ignore = true;
                            break;
                        }
                    }

                    // if it was not specified in DD, add it to connector-descriptor
                    if (!ignore) {
                        String credentialInterfaceName = getCredentialInterfaceName(auth.credentialInterface());
                        AuthMechanism authM = new AuthMechanism(auth.description(), authMechInt,
                                credentialInterfaceName);
                        ora.addAuthMechanism(authM);
                    }
                }
            }
            //}

            //TODO V3 care should be taken that only one annotation sets it
            //TODO V3 so that the "authSupportSet" logic is not made void
            if (!ora.isReauthenticationSupportSet()) {
                ora.setReauthenticationSupport(connector.reauthenticationSupport());
            }


            // merge DD and annotation entries of security-permission
            //if (desc.getSecurityPermissions().size() == 0) {
            SecurityPermission[] perms = connector.securityPermissions();
            if (perms != null && perms.length > 0) {
                for (SecurityPermission perm : perms) {
                    boolean ignore = false;
                    // check whether the same permission is defined in DD also,
                    // though it does not make any functionality difference except possible
                    // "Description" change
                    Set ddSecurityPermissions = desc.getSecurityPermissions();
                    for (Object o : ddSecurityPermissions) {
                        com.sun.enterprise.deployment.SecurityPermission ddSecurityPermission =
                                (com.sun.enterprise.deployment.SecurityPermission) o;
                        if (ddSecurityPermission.getPermission().equals(perm.permissionSpec())) {
                            ignore = true;
                            break;
                        }
                    }

                    // if it was not specified in DD, add it to connector-descriptor
                    if (!ignore) {
                        com.sun.enterprise.deployment.SecurityPermission sp =
                                new com.sun.enterprise.deployment.SecurityPermission();
                        sp.setPermission(perm.permissionSpec());
                        sp.setDescription(perm.description());
                        desc.addSecurityPermission(sp);
                    }
                }
            }
            //}

            //TODO V3 care should be taken that only one annotation sets it
            //TODO V3 so that the "authSupportSet" logic is not made void
            if (!ora.isTransactionSupportSet()) {
                ora.setTransactionSupport(connector.transactionSupport().toString());
            }

            //merge the DD & annotation specified values of required-inflow-contexts
            //merge involves simple union of class-names of inflow-contexts of DD and annotation
            // TODO V3 due to the above approach, its not possible to switch off one of the required-inflow-contexts ?
            // TODO V3 need to check support and throw exception ?

            //if(desc.getRequiredInflowContexts().size() == 0){
            Class<? extends WorkContext>[] requiredInflowContexts = connector.requiredWorkContexts();
            if (requiredInflowContexts != null) {
                for (Class<? extends WorkContext> ic : requiredInflowContexts) {
                    desc.addRequiredWorkContext(ic.getName());
                }
            }
            //}

            // TODO V3 Do not detect ResourceAdapter class if one is already defined in the descriptor
            // TODO V3 Refer the comments on top of the method for unhandled cases.
            if (desc.getResourceAdapterClass().equals("")) {
                Class c = (Class) element.getAnnotatedElement();
                String targetClassName = c.getName();
                if (javax.resource.spi.ResourceAdapter.class.isAssignableFrom(c)) {
                    desc.setResourceAdapterClass(targetClassName);
                }
            }
        } else {
            String logMessage = "Not a rar bundle context";
            return getFailureResult(element, logMessage, true);
        }
        List<Class<? extends Annotation>> list = new ArrayList<Class<? extends Annotation>>();
        list.add(getAnnotationType());
/*
        list.add(SecurityPermission.class);
        list.add(AuthenticationMechanism.class);
*/
        return getSuccessfulProcessedResult(list);
    }


    //TODO V3, move to outbound ra ?
    private String getCredentialInterfaceName(AuthenticationMechanism.CredentialInterface ci) {
        if (ci.equals(AuthenticationMechanism.CredentialInterface.GenericCredential)) {
            return GenericCredential.class.getName();
        } else if (ci.equals(AuthenticationMechanism.CredentialInterface.GSSCredential)) {
            return GSSCredential.class.getName(); //TODO validate ?
        } else if (ci.equals(AuthenticationMechanism.CredentialInterface.PasswordCredential)) {
            return PasswordCredential.class.getName();
        }
        throw new RuntimeException("Invalid credential interface :  " + ci);
    }


    public String convertStringArrayToStringBuffer(String[] stringArray) {
        StringBuffer result = new StringBuffer();
        if (stringArray != null) {
            for (String string : stringArray) {
                result.append(string);
            }
        }
        return result.toString();
    }

    /**
     * @return a default processed result
     */
    protected HandlerProcessingResult getSuccessfulProcessedResult(List<Class<? extends Annotation>> annotationTypes) {

        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();

        for(Class<? extends Annotation> annotation : annotationTypes){
            result.addResult(annotation, ResultType.PROCESSED);
        }
        return result;
    }


    public Class<? extends Annotation>[] getTypeDependencies() {
        return null;
    }

    public OutboundResourceAdapter getOutbound(ConnectorDescriptor desc) {
        if (!desc.getOutBoundDefined()) {
            desc.setOutboundResourceAdapter(new OutboundResourceAdapter());
        }
        return desc.getOutboundResourceAdapter();
    }

    private HandlerProcessingResultImpl getFailureResult(AnnotationInfo element, String message, boolean doLog) {
        HandlerProcessingResultImpl result = new HandlerProcessingResultImpl();
        result.addResult(getAnnotationType(), ResultType.FAILED);
        if (doLog) {
            Class c = (Class) element.getAnnotatedElement();
            String className = c.getName();
            //TODO V3 logStrings
            logger.log(Level.WARNING, "failed to handle annotation [ " + element.getAnnotation() + " ]" +
                    " on class [ " + className + " ], reason : " + message);
        }
        return result;
    }
}
